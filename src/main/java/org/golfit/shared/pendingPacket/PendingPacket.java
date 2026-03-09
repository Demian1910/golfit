package org.golfit.shared.pendingPacket;


public class PendingPacket {
    public final String packet;
    public long sentAt;
    public int retryCount = 0;

    public PendingPacket(String packet, long sentAt) {
        this.packet = packet;
        this.sentAt = sentAt;
    }
}