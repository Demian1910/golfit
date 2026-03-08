package org.golfit.shared.protocol;

import org.golfit.shared.packetParser.PacketParser;

public class Protocol {
    public static final byte PKG_SYS_CONNECT        = 0x01;
    public static final byte PKG_SYS_CONNECTED      = 0x02;
    public static final byte PKG_SYS_DISCONNECT     = 0x03;

    // ping
    public static final byte PKG_SYS_PING           = 0x04;
    public static final byte PKG_SYS_PONG           = 0x05;

    // lobby
    public static final byte PKG_SYS_CREATE_LOBBY   = 0x06;
    public static final byte PKG_SYS_LOBBY_CREATED  = 0x07;
    public static final byte PKG_SYS_JOIN_LOBBY     = 0x08;
    public static final byte PKG_SYS_LOBBY_JOINED   = 0x09;
    public static final byte PKG_SYS_LEAVE_LOBBY    = 0x0A;
    public static final byte PKG_SYS_LOBBY_LIST     = 0x0B;

    // chat
    public static final byte PKG_SYS_CHAT           = 0x0C;
    public static final byte PKG_SYS_CHAT_PRIVATE   = 0x0D;

    // movement
    public static final byte PKG_MOVE_INPUT         = 0x0E;
    public static final byte PKG_MOVE_UPDATE        = 0x0F;

    // protocol
    public static final byte PKG_SYS_ACK            = 0x10;

    // channels
    public static final byte CH_SYSTEM   = 0;
    public static final byte CH_MOVEMENT = 1;
    public static final byte CH_CHAT     = 2;

    // ids
    public static final int ID_INVALID   = 0;
    public static final int ID_SERVER    = 1;
    public static final int ID_BROADCAST = -1;

    @FunctionalInterface
    public interface SendFunction {
        void send(byte[] data) throws Exception;
    }

    @FunctionalInterface
    public interface PacketDispatcher {
        void dispatch(PacketParser.Packet packet) throws Exception;
    }
}
