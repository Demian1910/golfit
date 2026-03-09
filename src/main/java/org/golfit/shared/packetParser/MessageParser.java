package org.golfit.shared.packetParser;

import org.golfit.shared.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import static org.golfit.shared.protocol.Protocol.FIELD_COUNT;

public class MessageParser {
    private static final Logger logger = LoggerFactory.getLogger(MessageParser.class);

    private static final int FIELD_TYPE     = 0;
    private static final int FIELD_CHANNEL  = 1;
    private static final int FIELD_SENDER   = 2;
    private static final int FIELD_SEQUENCE = 3;
    private static final int FIELD_PAYLOAD  = 4;

    public static class Message {
        public String type;
        public String channel;
        public int senderId;
        public int sequence;
        public String payload;
    }

    public static Message parseMessage(String raw) {
        if(raw == null || raw.isEmpty()) {
            logger.error("Invalid packet format: null or empty string");
            return null;
        }
        raw = raw.trim();
        String[] parts = raw.split("\\|", 5);

        if(parts.length != FIELD_COUNT) {
            logger.error("Invalid packet format: " + raw);
            logger.info("Expected format: <type>|<channel>|<senderId>|<sequence>|<payload>\n");
            return null;
        }
        try {
            Message message = new Message();
            message.type = parts[FIELD_TYPE];
            message.channel = parts[FIELD_CHANNEL];
            message.senderId = Integer.parseInt(parts[FIELD_SENDER]);
            message.sequence = Integer.parseInt(parts[FIELD_SEQUENCE]);
            message.payload = parts[FIELD_PAYLOAD];
            return message;
        } catch (Exception e) {
            logger.error("Error parsing packet: " + e.getMessage());
            return null;
        }
    }
}
