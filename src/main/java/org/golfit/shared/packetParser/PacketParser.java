package org.golfit.shared.packetParser;

import java.nio.ByteBuffer;

public class PacketParser {
    public static class Packet {
        public byte type;
        public byte channel;
        public int senderId;
        public int sequence;
        public byte[] payload;
    }

    public static Packet parsePacket(byte[] raw) {
        if (raw == null || raw.length < 10) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(raw);
        Packet packet = new Packet();
        packet.type = buffer.get();
        packet.channel = buffer.get();
        packet.senderId = buffer.getInt();
        packet.sequence = buffer.getInt();

        if (buffer.remaining() > 0) {
            packet.payload = new byte[buffer.remaining()];
            buffer.get(packet.payload);
        } else {
            packet.payload = null;
        }
        return packet;
    }
}
