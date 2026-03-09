package org.golfit.shared.protocol.channelManager;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.packetBuilder.PacketBuilder;
import org.golfit.shared.packetParser.PacketParser;
import org.golfit.shared.pendingPacket.PendingPacket;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager {
    private final Map<Byte, Channel> channels = new HashMap<>();
    private final Protocol.SendFunction sender;
    private final Protocol.PacketDispatcher dispatcher;
    private final Logger logger = LoggerFactory.getLogger(ChannelManager.class);


    public ChannelManager(Protocol.SendFunction sender, Protocol.PacketDispatcher dispatcher) {
        channels.put(Protocol.CH_SYSTEM, new Channel(Protocol.CH_SYSTEM, true));
        channels.put(Protocol.CH_CHAT, new Channel(Protocol.CH_CHAT, true));
        channels.put(Protocol.CH_MOVEMENT, new Channel(Protocol.CH_MOVEMENT, false));
        this.sender = sender;
        this.dispatcher = dispatcher;
    }


    public void send(byte channelId, byte packetType, int senderId, byte[] payload) throws Exception {
        Channel channel = channels.get(channelId);
        int sequence = channel.nextSequence();

        byte[] packet = PacketBuilder.buildPacket(packetType, channelId, senderId, sequence, payload);
        sender.send(packet);
        if(channel.reliable) {
            channel.pendingAck.put(sequence, new PendingPacket(packet, System.currentTimeMillis()));
        }
    }
    public void send(byte channelId, byte packetType, int senderId) throws Exception {
        Channel channel = channels.get(channelId);
        int sequence = channel.nextSequence();

        byte[] packet = PacketBuilder.buildPacket(packetType, channelId, senderId, sequence);
        sender.send(packet);
        if(channel.reliable) {
            channel.pendingAck.put(sequence, new PendingPacket(packet, System.currentTimeMillis()));
        }
    }

    public void tick() throws Exception {
        long time = System.currentTimeMillis();
        for(Channel channel : channels.values()) {
            if(!channel.reliable) continue;
            for(PendingPacket pending : channel.pendingAck.values()) {
                if(time - pending.sentAt > Constants.TIMEOUT_MS) {
                    sender.send(pending.packet);
                    pending.sentAt = time;
                }
            }
        }
    }

    public void onPacketReceived(byte[] raw) throws Exception {
        PacketParser.Packet packet = PacketParser.parsePacket(raw);

        if (packet == null) {
            logger.error("Packet is null");
            return;
        }

        Channel channel = channels.get(packet.channel);
        if (packet.type == Protocol.PKG_SYS_ACK) {
            channel.pendingAck.remove(packet.sequence);
            return;
        }

        if (channel.reliable) {
            boolean isNew = channel.isNewPacket(packet.sequence);
            if (!isNew) return;
            sendAck(packet.channel, packet.sequence, packet.senderId);
        }
        dispatcher.dispatch(packet);
    }
    private void sendAck(byte channelId, int ackSequence, int targetId) throws Exception {
        byte[] ack = PacketBuilder.buildPacket(Protocol.PKG_SYS_ACK, channelId, Protocol.ID_SERVER, ackSequence, null);
        sender.send(ack);
    }
}