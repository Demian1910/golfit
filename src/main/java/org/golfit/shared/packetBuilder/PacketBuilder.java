package org.golfit.shared.packetBuilder;

import java.nio.ByteBuffer;

public class PacketBuilder {

    public static byte[] buildPacket(byte type, byte channel, int senderId, int sequence, byte[] payload) {
        int size = 10 + (payload != null ? payload.length : 0);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(type);
        buffer.put(channel);
        buffer.putInt(senderId);
        buffer.putInt(sequence);
        if (payload != null) buffer.put(payload);
        return buffer.array();
    }
    public static byte[] buildPacket(byte type, byte channel, int senderId, int sequence) {
        int size = 10;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(type);
        buffer.put(channel);
        buffer.putInt(senderId);
        buffer.putInt(sequence);
        return buffer.array();
    }
}

