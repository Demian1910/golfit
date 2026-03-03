package org.golfit.shared.protocol;

public class Protocol {
    public static final byte PKG_SYS_CONNECT = 0x01;
    public static final byte PKG_SYS_DISCONNECT = 0x02;
    public static final byte PKG_SYS_PING = 0x03;
    public static final byte PKG_SYS_PONG = 0x04;
    public static final byte PKG_SYS_CREATE_LOBBY = 0x05;
    public static final byte PKG_SYS_JOIN_LOBBY = 0x06;
    public static final byte PKG_SYS_LEAVE_LOBBY = 0x07;
    public static final byte PKG_SYS_LOBBY_LIST = 0x08;

    public static final byte PKG_SYS_CHAT = 0x09;
    public static final byte PKG_SYS_CHAT_PRIVATE = 0x0A;

    public static final byte PKG_MOVE_INPUT = 0xB;
    public static final byte PKG_MOVE_UPDATE = 0xC;
    public static final byte PKG_SYS_ACK = 0xD;

    public static final byte CH_SYSTEM = 0;
    public static final byte CH_MOVEMENT = 1;
    public static final byte CH_CHAT = 2;

    public static final int ID_INVALID = 0;
    public static final int ID_SERVER = 1;
    public static final int ID_BROADCAST = -1;

    @FunctionalInterface
    public interface SendFunction {
        void send(byte[] data) throws Exception;
    }
}
