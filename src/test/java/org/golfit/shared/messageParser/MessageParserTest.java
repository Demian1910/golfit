package org.golfit.shared.messageParser;

import org.golfit.shared.messageParser.MessageParser.Message;
import org.golfit.shared.protocol.Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    @Test
    void parseMessage_validMessage() {
        Message msg = MessageParser.parseMessage("SYS_CONNECT|CH_SYSTEM|42|7|hello\n");
        assertNotNull(msg);
        assertEquals(Protocol.SYS_CONNECT, msg.type);
        assertEquals(Protocol.CH_SYSTEM, msg.channel);
        assertEquals(42, msg.senderId);
        assertEquals(7, msg.sequence);
        assertEquals("hello", msg.payload);
    }

    @Test
    void parseMessage_emptyPayload() {
        Message msg = MessageParser.parseMessage("SYS_PING|CH_PINGS|1|0|");
        assertNotNull(msg);
        assertEquals("", msg.payload);
    }

    @Test
    void parseMessage_payloadContainingDelimiters() {
        Message msg = MessageParser.parseMessage("SYS_CHAT_BROADCAST|CH_CHAT|5|3|a|b|c");
        assertNotNull(msg);
        assertEquals("a|b|c", msg.payload);
    }

    @Test
    void parseMessage_nullInput() {
        assertNull(MessageParser.parseMessage(null));
    }

    @Test
    void parseMessage_emptyString() {
        assertNull(MessageParser.parseMessage(""));
    }

    @Test
    void parseMessage_tooFewFields() {
        assertNull(MessageParser.parseMessage("SYS_CONNECT|CH_SYSTEM|42"));
    }

    @Test
    void parseMessage_invalidSenderId() {
        assertNull(MessageParser.parseMessage("SYS_CONNECT|CH_SYSTEM|notANumber|0|"));
    }

    @Test
    void parseMessage_invalidSequence() {
        assertNull(MessageParser.parseMessage("SYS_CONNECT|CH_SYSTEM|1|notANumber|"));
    }

    @Test
    void parseMessage_negativeSenderId() {
        Message msg = MessageParser.parseMessage("SYS_CONNECT|CH_SYSTEM|-1|0|");
        assertNotNull(msg);
        assertEquals(-1, msg.senderId);
    }

    @Test
    void parseMessage_trailingWhitespaceIsTrimmed() {
        Message msg = MessageParser.parseMessage("SYS_PING|CH_PINGS|1|0|  \n  ");
        assertNotNull(msg);
    }
}
