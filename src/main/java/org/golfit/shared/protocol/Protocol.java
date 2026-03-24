package org.golfit.shared.protocol;

import org.golfit.shared.messageParser.MessageParser;

/** Defines all protocol constants including packet types, channel identifiers, special IDs, and functional interfaces. */
public class Protocol {
    public static final String SYS_CONNECT        = "SYS_CONNECT";
    public static final String SYS_CONNECTED      = "SYS_CONNECTED";
    public static final String SYS_DISCONNECT     = "SYS_DISCONNECT";

    // ping
    public static final String SYS_PING           = "SYS_PING";
    public static final String SYS_PONG           = "SYS_PONG";

    // lobby
    public static final String SYS_LOBBY_CREATE   = "SYS_LOBBY_CREATE";
    public static final String SYS_LOBBY_CREATED  = "SYS_LOBBY_CREATED";
    public static final String SYS_LOBBY_JOIN     = "SYS_LOBBY_JOIN";
    public static final String SYS_LOBBY_JOINED   = "SYS_LOBBY_JOINED";
    public static final String SYS_LOBBY_LEAVE    = "SYS_LOBBY_LEAVE";
    public static final String SYS_LOBBY_LEFT     = "SYS_LOBBY_LEFT";
    public static final String SYS_LOBBY_LIST     = "SYS_LOBBY_LIST";
    public static final String SYS_LOBBY_INFO     =  "SYS_LOBBY_INFO";
    public static final String SYS_PLAYERS_LIST   = "SYS_PLAYERS_LIST";

    // chat
    public static final String SYS_CHAT_BROADCAST = "SYS_CHAT_BROADCAST";
    public static final String SYS_CHAT_PRIVATE   = "SYS_CHAT_PRIVATE";
    public static final String SYS_CHAT_LOBBY     = "SYS_CHAT_LOBBY";

    // movement
    public static final String  MOVE_INPUT         = "MOVE_INPUT";
    public static final String  MOVE_UPDATE        = "MOVE_UPDATE";

    // protocol
    public static final String SYS_ACK             = "SYS_ACK";

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
