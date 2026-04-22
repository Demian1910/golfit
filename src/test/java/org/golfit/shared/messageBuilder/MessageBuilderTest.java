package org.golfit.shared.messageBuilder;

import org.golfit.shared.protocol.Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBuilderTest {

    @Test
    void buildMessage_containsAllFieldsInOrder() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, 42, 7, "hello");
        assertEquals("SYS_CONNECT|CH_SYSTEM|42|7|hello\n", msg);
    }

    @Test
    void buildMessage_emptyPayload() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_PING, Protocol.CH_PINGS, 1, 0, "");
        assertEquals("SYS_PING|CH_PINGS|1|0|\n", msg);
    }

    @Test
    void buildMessage_payloadWithDelimiter() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_CHAT_BROADCAST, Protocol.CH_CHAT, 5, 3, "a|b|c");
        assertEquals("SYS_CHAT_BROADCAST|CH_CHAT|5|3|a|b|c\n", msg);
    }

    @Test
    void buildMessage_broadcastId() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_CONNECT, Protocol.CH_SYSTEM, Protocol.ID_BROADCAST, 0, "");
        assertTrue(msg.contains("|" + Protocol.ID_BROADCAST + "|"));
    }

    @Test
    void buildMessage_movementPayload() {
        String msg = MessageBuilder.buildMessage(
                Protocol.MOVE_UPDATE, Protocol.CH_MOVEMENT, 10, 0, "1:2.00,3.00;");
        assertEquals("MOVE_UPDATE|CH_MOVEMENT|10|0|1:2.00,3.00;\n", msg);
    }

    @Test
    void buildMessage_endsWithTerminator() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_ACK, Protocol.CH_SYSTEM, 1, 5, "");
        assertEquals('\n', msg.charAt(msg.length() - 1));
    }

    @Test
    void buildMessage_largeSequenceNumber() {
        String msg = MessageBuilder.buildMessage(
                Protocol.SYS_PING, Protocol.CH_PINGS, 1, Integer.MAX_VALUE, "");
        assertTrue(msg.contains(String.valueOf(Integer.MAX_VALUE)));
    }
}
