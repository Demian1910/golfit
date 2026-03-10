package org.golfit.shared.protocol;

import org.golfit.shared.messageParser.MessageParser;

public class Protocol {
    public static final String SYS_CONNECT        = "SYS_CONNECT";
    public static final String SYS_CONNECTED      = "SYS_CONNECTED";
    public static final String SYS_DISCONNECT     = "SYS_DISCONNECT";

    // ping
    public static final String SYS_PING           = "SYS_PING";
    public static final String SYS_PONG           = "SYS_PONG";

    // lobby
    public static final String SYS_CREATE_LOBBY   = "SYS_CREATE_LOBBY";
    public static final String SYS_LOBBY_CREATED  = "SYS_LOBBY_CREATED";
    public static final String SYS_JOIN_LOBBY     = "SYS_JOIN_LOBBY";
    public static final String SYS_LOBBY_JOINED   = "SYS_LOBBY_JOINED";
    public static final String SYS_LEAVE_LOBBY    = "SYS_LEAVE_LOBBY";
    public static final String SYS_LOBBY_LIST     = "SYS_LOBBY_LIST";

    // chat
    public static final String SYS_CHAT           = "SYS_CHAT";
    public static final String SYS_CHAT_PRIVATE   = "SYS_CHAT_PRIVATE";

    // movement
    public static final String  MOVE_INPUT         = "MOVE_INPUT";
    public static final String  MOVE_UPDATE        = "MOVE_UPDATE";

    // protocol
    public static final String SYS_ACK            = "SYS_ACK";

    // channels
    public static final String CH_SYSTEM   = "CH_SYSTEM";
    public static final String CH_MOVEMENT = "CH_MOVEMENT";
    public static final String CH_CHAT     = "CH_CHAT";
    public static final String CH_PINGS    = "CH_PINGS";

    // ids
    public static final int ID_INVALID   = 0;
    public static final int ID_SERVER    = 1;
    public static final int ID_BROADCAST = -1;

    public static final char DELIMITER = '|';
    public static final char TERMINATOR = '\n';
    public static final int FIELD_COUNT = 5;

    @FunctionalInterface
    public interface SendFunction {
        void send(String data) throws Exception;
    }

    @FunctionalInterface
    public interface PacketDispatcher {
        void dispatch(MessageParser.Message message) throws Exception;
    }
}
