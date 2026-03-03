package org.golfit.shared.protocol.channelManager;

import org.golfit.shared.packetBuilder.PacketBuilder;
import org.golfit.shared.packetParser.PacketParser;
import org.golfit.shared.pendingPacket.PendingPacket;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channel.Channel;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager {
    private final Map<Byte, Channel> channels = new HashMap<>();
    private final Protocol.SendFunction sender;

    public ChannelManager(Protocol.SendFunction sender) {
        channels.put(Protocol.CH_SYSTEM, new Channel(Protocol.CH_SYSTEM, true));
        channels.put(Protocol.CH_CHAT, new Channel(Protocol.CH_CHAT, true));
        channels.put(Protocol.CH_MOVEMENT, new Channel(Protocol.CH_MOVEMENT, false));
        this.sender = sender;
    }

    public void send(byte channelId, byte packetType, int senderId, byte[] payload) throws Exception {
        Channel channel = channels.get(channelId);
        int sequence = channel.nextSequence();

        byte[] packet = PacketBuilder.buildPackage(packetType, channelId, senderId, sequence, payload);
        sender.send(packet);
        if(channel.reliable) {
            channel.pendingAck.put(sequence, new PendingPacket(packet, System.currentTimeMillis()));
        }
    }

    public void onPackageReceived(byte[] raw) throws Exception {
        PacketParser.Header header = PacketParser.parsePackage(raw);
        if(header == null) return;

        Channel channel = channels.get(header.channel);
        if(channel.reliable) {
            if(channel.isNewPacket(header.sequence)) return;
            sendAck(header.channel, header.sequence, header.senderId);
        }

        if(header.type == Protocol.PKG_SYS_ACK) {
            channel.pendingAck.remove(header.sequence);
            return;
        }
//        dispatch(header);
    }
    private void sendAck(byte channelId, int ackSequence, int targetId) throws Exception {
        byte[] ack = PacketBuilder.buildPackage(Protocol.PKG_SYS_ACK, channelId, Protocol.ID_SERVER, ackSequence, null);
        sender.send(ack);
    }
}
