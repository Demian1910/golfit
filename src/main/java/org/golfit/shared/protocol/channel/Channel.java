package org.golfit.shared.protocol.channel;

import org.golfit.shared.pendingPacket.PendingPacket;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tracks per-channel state including sequence numbers and pending acknowledgements for reliable delivery. */
public class Channel {
    public final String id;
    public final boolean reliable;

    private int nextSendSequence = 0;
    private int nextExpectedSequence = 0;

    public final Map<Integer, PendingPacket> pendingAck = new LinkedHashMap<>();

    public Channel(String id, boolean reliable) {
        this.id = id;
        this.reliable = reliable;
    }

    /** Returns the next send sequence number. Reliable channels increment; unreliable always return 0. */
    public int nextSequence() {
        return reliable ? nextSendSequence++ : 0;
    }

    /** Returns the next expected receive sequence number. */
    public int nextExpectedSequence() {
        return nextExpectedSequence;
    }

    /** Checks if a packet is new based on its sequence number. Advances the expected sequence on match. */
    public boolean isNewPacket(int seq) {
        if (!reliable) return true;
        if (seq < nextExpectedSequence) return false;
        if (seq == nextExpectedSequence) {
            nextExpectedSequence++;
            return true;
        }
        return false;
    }
    /** Resets all sequence counters and clears pending acknowledgements. */
    public void reset() {
        nextSendSequence = 0;
        nextExpectedSequence = 0;
        pendingAck.clear();
    }
}
