package org.golfit.shared.protocol.channel;

import org.golfit.shared.pendingPacket.PendingPacket;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public int nextSequence() {
        return reliable ? nextSendSequence++ : 0;
    }

    public int nextExpectedSequence() {
        return nextExpectedSequence;
    }

    public boolean isNewPacket(int seq) {
        if (!reliable) return true;
        if (seq < nextExpectedSequence) return false;
        if (seq == nextExpectedSequence) {
            nextExpectedSequence++;
            return true;
        }
        return false;
    }
    public void reset() {
        nextSendSequence = 0;
        nextExpectedSequence = 0;
        pendingAck.clear();
    }
}
