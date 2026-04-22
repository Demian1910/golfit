package org.golfit.shared.protocol.channelManager;

import org.golfit.shared.messageParser.MessageParser;
import org.golfit.shared.protocol.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChannelManagerTest {

    private List<String> sentMessages;
    private List<MessageParser.Message> dispatchedMessages;
    private ChannelManager manager;

    @BeforeEach
    void setUp() {
        sentMessages = new ArrayList<>();
        dispatchedMessages = new ArrayList<>();
        manager = new ChannelManager(
                sentMessages::add,
                dispatchedMessages::add,
                42
        );
    }

    @Test
    void send_reliableChannel_sendsMessage() throws Exception {
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "hello");
        assertEquals(1, sentMessages.size());
        assertTrue(sentMessages.get(0).startsWith("SYS_CONNECT|CH_SYSTEM|42|0|hello"));
    }

    @Test
    void send_reliableChannel_incrementsSequence() throws Exception {
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "a");
        manager.send(Protocol.SYS_PING, Protocol.CH_SYSTEM, 42, "b");
        assertTrue(sentMessages.get(0).contains("|0|"));
        assertTrue(sentMessages.get(1).contains("|1|"));
    }

    @Test
    void send_unreliableChannel_sequenceAlwaysZero() throws Exception {
        manager.send(Protocol.MOVE_INPUT, Protocol.CH_MOVEMENT, 42, "x");
        manager.send(Protocol.MOVE_INPUT, Protocol.CH_MOVEMENT, 42, "y");
        assertTrue(sentMessages.get(0).contains("|0|"));
        assertTrue(sentMessages.get(1).contains("|0|"));
    }

    @Test
    void onPacketReceived_dispatchesValidMessage() throws Exception {
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|hello\n");
        assertEquals(1, dispatchedMessages.size());
        assertEquals("hello", dispatchedMessages.get(0).payload);
    }

    @Test
    void onPacketReceived_reliableChannel_sendsAck() throws Exception {
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|hello\n");
        assertEquals(1, sentMessages.size());
        assertTrue(sentMessages.get(0).contains(Protocol.SYS_ACK));
        assertTrue(sentMessages.get(0).contains("CH_SYSTEM"));
    }

    @Test
    void onPacketReceived_unreliableChannel_noAck() throws Exception {
        manager.onPacketReceived("MOVE_UPDATE|CH_MOVEMENT|10|0|data\n");
        assertTrue(sentMessages.isEmpty());
        assertEquals(1, dispatchedMessages.size());
    }

    @Test
    void onPacketReceived_duplicateReliable_discarded() throws Exception {
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|first\n");
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|duplicate\n");
        assertEquals(1, dispatchedMessages.size());
        assertEquals("first", dispatchedMessages.get(0).payload);
    }

    @Test
    void onPacketReceived_ack_removesPendingPacket() throws Exception {
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "test");
        sentMessages.clear();
        manager.onPacketReceived("SYS_ACK|CH_SYSTEM|10|0|\n");
        assertTrue(dispatchedMessages.isEmpty());
    }

    @Test
    void onPacketReceived_invalidMessage_noDispatch() throws Exception {
        manager.onPacketReceived("garbage");
        assertTrue(dispatchedMessages.isEmpty());
    }

    @Test
    void onPacketReceived_unknownChannel_noDispatch() throws Exception {
        manager.onPacketReceived("SYS_CONNECT|CH_UNKNOWN|10|0|test\n");
        assertTrue(dispatchedMessages.isEmpty());
    }

    @Test
    void resetChannels_allowsSequenceRestart() throws Exception {
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "a");
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "b");
        manager.resetChannels();
        sentMessages.clear();
        manager.send(Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, "c");
        assertTrue(sentMessages.get(0).contains("|0|"));
    }

    @Test
    void updateLocalId_changesAckSenderId() throws Exception {
        manager.updateLocalId(99);
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|hello\n");
        assertTrue(sentMessages.get(0).contains("|99|"));
    }

    @Test
    void onPacketReceived_reliableInOrder_allDispatched() throws Exception {
        manager.onPacketReceived("SYS_CONNECT|CH_SYSTEM|10|0|a\n");
        manager.onPacketReceived("SYS_PING|CH_SYSTEM|10|1|b\n");
        manager.onPacketReceived("SYS_PONG|CH_SYSTEM|10|2|c\n");
        assertEquals(3, dispatchedMessages.size());
    }

    @Test
    void onPacketReceived_chatChannel_reliable() throws Exception {
        manager.onPacketReceived("SYS_CHAT_BROADCAST|CH_CHAT|10|0|hi\n");
        assertEquals(1, sentMessages.size());
        assertTrue(sentMessages.get(0).contains(Protocol.SYS_ACK));
        assertEquals(1, dispatchedMessages.size());
    }

    @Test
    void onPacketReceived_pingChannel_unreliable() throws Exception {
        manager.onPacketReceived("SYS_PING|CH_PINGS|10|0|\n");
        assertTrue(sentMessages.isEmpty());
    }
}
