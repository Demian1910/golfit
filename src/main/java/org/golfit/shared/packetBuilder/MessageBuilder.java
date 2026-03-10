package org.golfit.shared.packetBuilder;

import java.nio.ByteBuffer;

import static org.golfit.shared.protocol.Protocol.DELIMITER;
import static org.golfit.shared.protocol.Protocol.TERMINATOR;

public class MessageBuilder {


    public static String buildMessage(String type, String channel, int senderId, int sequence, String payload) {
        StringBuilder builder = new StringBuilder();
        builder.append(type).append(DELIMITER);
        builder.append(channel).append(DELIMITER);
        builder.append(senderId).append(DELIMITER);
        builder.append(sequence).append(DELIMITER);
        if(payload.length() > 0) {
            builder.append(payload);
        }
        builder.append(TERMINATOR);
        return builder.toString();
    }
}

