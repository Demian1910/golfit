package org.golfit.shared.protocol.channelManager;

import org.golfit.shared.constants.Constants;
import org.golfit.shared.messageBuilder.MessageBuilder;
import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.pendingPacket.PendingPacket;
import org.golfit.shared.protocol.Protocol;
import org.golfit.shared.protocol.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages logical communication channels between a client and the server.
 *
 * <p>The {@code ChannelManager} is the core of the GolfIt reliable delivery system.
 * It maintains four channels ({@code CH_SYSTEM}, {@code CH_CHAT}, {@code CH_MOVEMENT},
 * {@code CH_PINGS}), two of which are reliable and two unreliable:
 *
 * <ul>
 *   <li><b>Reliable channels</b> ({@code CH_SYSTEM}, {@code CH_CHAT}): packets are
 *       acknowledged by the receiver. Unacknowledged packets are retried every
 *       {@code TIMEOUT_MS} milliseconds up to {@code MAX_RETRIES} times.</li>
 *   <li><b>Unreliable channels</b> ({@code CH_MOVEMENT}, {@code CH_PINGS}): packets
 *       are sent once with no acknowledgement or retry.</li>
 * </ul>
 *
 * <p>The same {@code ChannelManager} class is used on both the client and server side.
 * The {@code sender} lambda is the only difference — on the client it sends to the server,
 * on the server it sends back to a specific client.
 */
public class ChannelManager {
    private final Map<String, Channel> channels = new HashMap<>();
    private final Protocol.SendFunction sender;
    private final Protocol.PacketDispatcher dispatcher;
    private final Logger logger = LoggerFactory.getLogger(ChannelManager.class);


    /**
     * Constructs a {@code ChannelManager} and initializes all four protocol channels.
     *
     * @param sender     function that sends a raw message string over the network
     * @param dispatcher function that dispatches a parsed message to the appropriate handler
     */
    public ChannelManager(Protocol.SendFunction sender, Protocol.PacketDispatcher dispatcher) {
        channels.put(Protocol.CH_SYSTEM, new Channel(Protocol.CH_SYSTEM, true));
        channels.put(Protocol.CH_CHAT, new Channel(Protocol.CH_CHAT, true));
        channels.put(Protocol.CH_MOVEMENT, new Channel(Protocol.CH_MOVEMENT, false));
        channels.put(Protocol.CH_PINGS, new Channel(Protocol.CH_PINGS, false));
        this.sender = sender;
        this.dispatcher = dispatcher;
    }

    /**
     * Sends a message on the specified channel.
     *
     * <p>Assigns the next sequence number for the channel, builds the message string,
     * and transmits it via {@link #sender}. If the channel is reliable, the packet is
     * stored in the channel's {@code pendingAck} map and will be retried by {@link #tick()}
     * until acknowledged or the retry limit is reached.
     *
     * @param packetType the message type constant (e.g. {@code Protocol.SYS_CONNECT})
     * @param channelId  the channel to send on (e.g. {@code Protocol.CH_SYSTEM})
     * @param senderId   the ID of the sender
     * @param payload    the message payload; may be empty but not null
     * @throws Exception if the sender throws during transmission
     */
    public void send(String packetType, String channelId, int senderId, String payload) throws Exception {
        Channel channel = channels.get(channelId);
        int sequence = channel.nextSequence();

        String message = MessageBuilder.buildMessage(packetType, channelId, senderId, sequence, payload);
        sender.send(message);
        if(channel.reliable) {
            channel.pendingAck.put(sequence, new PendingPacket(message, System.currentTimeMillis()));
        }
    }

    /**
     * Drives the retry logic for reliable channels. Should be called every ~100ms.
     *
     * <p>For each reliable channel, iterates over all unacknowledged packets. If a packet
     * has been waiting longer than {@code TIMEOUT_MS}:
     * <ul>
     *   <li>If {@code retryCount >= MAX_RETRIES}, the packet is dropped</li>
     *   <li>Otherwise, it is retransmitted and its retry counter is incremented</li>
     * </ul>
     *
     * @throws Exception if the sender throws during retransmission
     */
    public void tick() throws Exception {
        long time = System.currentTimeMillis();
        for (Channel channel : channels.values()) {
            if (!channel.reliable) continue;

            List<Integer> toRemove = new ArrayList<>();

            for (Map.Entry<Integer, PendingPacket> entry : channel.pendingAck.entrySet()) {
                PendingPacket pending = entry.getValue();
                if (time - pending.sentAt > Constants.TIMEOUT_MS) {
                    if (pending.retryCount >= Constants.MAX_RETRIES) {
                        toRemove.add(entry.getKey());
                        continue;
                    }
                    sender.send(pending.packet);
                    pending.sentAt = time;
                    pending.retryCount++;
                }
            }

            toRemove.forEach(channel.pendingAck::remove);
        }
    }

    /**
     * Processes a raw incoming packet string.
     *
     * <p>Parses the raw string into a {@link MessageParser.Message}. Then:
     * <ul>
     *   <li>If the message is {@code SYS_ACK}, removes the matching sequence from
     *       {@code pendingAck} and returns — no further processing.</li>
     *   <li>If the channel is reliable, checks whether this is a new (non-duplicate) packet
     *       via {@link Channel#isNewPacket}. Duplicates are silently discarded.
     *       For new packets, an ACK is sent back automatically.</li>
     *   <li>Dispatches the message to the registered {@link #dispatcher}.</li>
     * </ul>
     *
     * @param raw the raw UTF-8 message string received from the network
     * @throws Exception if ACK sending or dispatching throws
     */
    public void onPacketReceived(String raw) throws Exception {
        MessageParser.Message message = MessageParser.parseMessage(raw);
        logger.info("Received message: " + raw.trim());

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
        dispatcher.dispatch(message);
    }

    /**
     * Sends a {@code SYS_ACK} back to the sender for a received packet on a reliable channel.
     *
     * <p>The ACK carries the same sequence number as the packet being acknowledged,
     * so the sender can remove it from its {@code pendingAck} map.
     *
     * @param channelId   the channel the original packet arrived on
     * @param ackSequence the sequence number being acknowledged
     * @param targetId    the ID of the original sender (unused in routing, kept for protocol symmetry)
     * @throws Exception if the sender throws during transmission
     */
    private void sendAck(String channelId, int ackSequence, int targetId) throws Exception {
        String ack = MessageBuilder.buildMessage(Protocol.SYS_ACK, channelId, Protocol.ID_SERVER, ackSequence, "");
        logger.info("Sending ACK for sequence: " + ackSequence + " on channel: " + channelId);
        sender.send(ack);
    }

    /**
     * Resets all channels to their initial state.
     *
     * <p>Clears sequence counters, pending ACKs, and seen-sequence history for every channel.
     * Should be called on reconnect to allow sequence numbers to restart from 0.
     */
    public void resetChannels() {
        for(Channel channel : channels.values()) {
            channel.reset();
        }
    }
}