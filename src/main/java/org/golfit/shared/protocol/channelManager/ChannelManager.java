package org.golfit.shared.protocol.channelManager;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.packetBuilder.MessageBuilder;
import org.golfit.shared.packetParser.MessageParser;
import org.golfit.shared.pendingPacket.PendingPacket;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager {
    private final Map<String, Channel> channels = new HashMap<>();
    private final Protocol.SendFunction sender;
    private final Protocol.PacketDispatcher dispatcher;
    private final Logger logger = LoggerFactory.getLogger(ChannelManager.class);


    public ChannelManager(Protocol.SendFunction sender, Protocol.PacketDispatcher dispatcher) {
        channels.put(Protocol.CH_SYSTEM, new Channel(Protocol.CH_SYSTEM, true));
        channels.put(Protocol.CH_CHAT, new Channel(Protocol.CH_CHAT, true));
        channels.put(Protocol.CH_MOVEMENT, new Channel(Protocol.CH_MOVEMENT, false));
        channels.put(Protocol.CH_PINGS, new Channel(Protocol.CH_PINGS, false));
        this.sender = sender;
        this.dispatcher = dispatcher;
    }


    public void send(String packetType, String channelId, int senderId, String payload) throws Exception {
        Channel channel = channels.get(channelId);
        int sequence = channel.nextSequence();

        String message = MessageBuilder.buildMessage(packetType, channelId, senderId, sequence, payload);
        logger.info("Message bytes: " + Arrays.toString(message.getBytes()));
        sender.send(message);
        if(channel.reliable) {
            channel.pendingAck.put(sequence, new PendingPacket(message, System.currentTimeMillis()));
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

    public void onPacketReceived(String raw) throws Exception {
        MessageParser.Message message = MessageParser.parseMessage(raw);
        logger.info("Received message: " + raw);

        if (message == null) {
            logger.error("Message is null");
            return;
        }

        Channel channel = channels.get(message.channel);
        if (message.type.equals(Protocol.SYS_ACK)) {
            channel.pendingAck.remove(message.sequence);
            return;
        }

        if (channel.reliable) {
            boolean isNew = channel.isNewPacket(message.sequence);
            if (!isNew) return;
            sendAck(message.channel, message.sequence, message.senderId);
        }
        logger.info("Received packet: " + message.type);
        logger.info("Received packet: " + message.channel);
        logger.info("Received packet: " + message.senderId);
        logger.info("Received packet: " + message.payload);
        dispatcher.dispatch(message);
    }
    private void sendAck(String channelId, int ackSequence, int targetId) throws Exception {
        String ack = MessageBuilder.buildMessage(Protocol.SYS_ACK, channelId, Protocol.ID_SERVER, ackSequence, "");
        logger.info("Sending ack: " + ack);
        sender.send(ack);
    }
}