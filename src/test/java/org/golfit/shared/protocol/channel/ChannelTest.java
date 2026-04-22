package org.golfit.shared.protocol.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChannelTest {

    @Test
    void reliableChannel_sequenceIncrements() {
        Channel ch = new Channel("CH_SYSTEM", true);
        assertEquals(0, ch.nextSequence());
        assertEquals(1, ch.nextSequence());
        assertEquals(2, ch.nextSequence());
    }

    @Test
    void unreliableChannel_sequenceAlwaysZero() {
        Channel ch = new Channel("CH_MOVEMENT", false);
        assertEquals(0, ch.nextSequence());
        assertEquals(0, ch.nextSequence());
        assertEquals(0, ch.nextSequence());
    }

    @Test
    void isNewPacket_reliable_acceptsInOrder() {
        Channel ch = new Channel("CH_SYSTEM", true);
        assertTrue(ch.isNewPacket(0));
        assertTrue(ch.isNewPacket(1));
        assertTrue(ch.isNewPacket(2));
    }

    @Test
    void isNewPacket_reliable_rejectsDuplicate() {
        Channel ch = new Channel("CH_SYSTEM", true);
        assertTrue(ch.isNewPacket(0));
        assertFalse(ch.isNewPacket(0));
    }

    @Test
    void isNewPacket_reliable_rejectsOldSequence() {
        Channel ch = new Channel("CH_SYSTEM", true);
        ch.isNewPacket(0);
        ch.isNewPacket(1);
        assertFalse(ch.isNewPacket(0));
    }

    @Test
    void isNewPacket_reliable_rejectsFutureGap() {
        Channel ch = new Channel("CH_SYSTEM", true);
        assertFalse(ch.isNewPacket(5));
    }

    @Test
    void isNewPacket_unreliable_alwaysTrue() {
        Channel ch = new Channel("CH_MOVEMENT", false);
        assertTrue(ch.isNewPacket(0));
        assertTrue(ch.isNewPacket(0));
        assertTrue(ch.isNewPacket(999));
    }

    @Test
    void reset_clearsSequences() {
        Channel ch = new Channel("CH_SYSTEM", true);
        ch.nextSequence();
        ch.nextSequence();
        ch.isNewPacket(0);
        ch.isNewPacket(1);
        ch.reset();
        assertEquals(0, ch.nextSequence());
        assertTrue(ch.isNewPacket(0));
    }

    @Test
    void reset_clearsPendingAck() {
        Channel ch = new Channel("CH_SYSTEM", true);
        ch.pendingAck.put(0, new org.golfit.shared.pendingPacket.PendingPacket("test", 0));
        assertFalse(ch.pendingAck.isEmpty());
        ch.reset();
        assertTrue(ch.pendingAck.isEmpty());
    }

    @Test
    void nextExpectedSequence_startsAtZero() {
        Channel ch = new Channel("CH_SYSTEM", true);
        assertEquals(0, ch.nextExpectedSequence());
    }

    @Test
    void nextExpectedSequence_advancesAfterIsNewPacket() {
        Channel ch = new Channel("CH_SYSTEM", true);
        ch.isNewPacket(0);
        assertEquals(1, ch.nextExpectedSequence());
        ch.isNewPacket(1);
        assertEquals(2, ch.nextExpectedSequence());
    }
}
