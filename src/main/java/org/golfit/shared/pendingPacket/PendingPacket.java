package org.golfit.shared.pendingPacket;


public class PendingPacket {
    public final byte[] packet;
    public long sentAt;

    public PendingPacket(byte[] packet, long sentAt) {
        this.packet = packet;
        this.sentAt = sentAt;
    }
}