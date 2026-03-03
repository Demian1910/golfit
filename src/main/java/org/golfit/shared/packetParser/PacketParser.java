package org.golfit.shared.packetParser;

import java.nio.ByteBuffer;

public class PacketParser {
    public static class Header {
        public byte type;
        public byte channel;
        public int senderId;
        public int sequence;
        public byte[] payload;
    }

    public static Header parsePackage(byte[] raw) {
        if(raw.length < 11) return null;
        ByteBuffer buffer = ByteBuffer.wrap(raw);
        Header h = new Header();
        h.type = buffer.get();
        h.channel = buffer.get();
        h.senderId = buffer.getInt();
        h.sequence = buffer.getInt();
        h.payload = new byte[buffer.remaining()];
        buffer.get(h.payload);
        return h;
    }
}
