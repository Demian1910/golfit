package org.golfit.shared.pendingPacket;


/** Represents a sent packet awaiting acknowledgement, tracking its send time and retry count. */
public class PendingPacket {
    public final String packet;
    public long sentAt;
    public int retryCount = 0;

    /**
     * Creates a new PendingPacket.
     *
     * @param packet the raw message string awaiting acknowledgement
     * @param sentAt the timestamp (millis) when the packet was last sent
     */
    public PendingPacket(String packet, long sentAt) {
        this.packet = packet;
        this.sentAt = sentAt;
    }
}