package org.golfit.shared.messageBuilder;

import static org.golfit.shared.protocol.Protocol.DELIMITER;
import static org.golfit.shared.protocol.Protocol.TERMINATOR;

/**
 * Builder class for messages.
 * Has method to encode messages into strings.
 * Builds messages for sending over the network.
 */
public class MessageBuilder {

    /** Encodes a message into a string. Used for sending messages over the network. */
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

