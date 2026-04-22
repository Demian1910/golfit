package org.golfit.shared.pendingPacket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PendingPacketTest {

    @Test
    void constructor_storesPacketAndTimestamp() {
        PendingPacket pp = new PendingPacket("SYS_CONNECT|CH_SYSTEM|1|0|\n", 5000L);
        assertEquals("SYS_CONNECT|CH_SYSTEM|1|0|\n", pp.packet);
        assertEquals(5000L, pp.sentAt);
    }

    @Test
    void retryCount_startsAtZero() {
        PendingPacket pp = new PendingPacket("test", 0);
        assertEquals(0, pp.retryCount);
    }

    @Test
    void retryCount_canBeIncremented() {
        PendingPacket pp = new PendingPacket("test", 0);
        pp.retryCount++;
        pp.retryCount++;
        assertEquals(2, pp.retryCount);
    }

    @Test
    void sentAt_canBeUpdated() {
        PendingPacket pp = new PendingPacket("test", 1000L);
        pp.sentAt = 3000L;
        assertEquals(3000L, pp.sentAt);
    }

    @Test
    void packet_isImmutable() {
        PendingPacket pp = new PendingPacket("original", 0);
        assertEquals("original", pp.packet);
    }
}
