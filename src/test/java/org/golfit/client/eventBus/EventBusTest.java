package org.golfit.client.eventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    private EventBus bus;

    @BeforeEach
    void setUp() throws Exception {
        var field = EventBus.class.getDeclaredField("listeners");
        field.setAccessible(true);
        bus = EventBus.get();
        ((java.util.Map<?, ?>) field.get(bus)).clear();
    }

    @Test
    void get_returnsSingletonInstance() {
        assertSame(EventBus.get(), EventBus.get());
    }

    @Test
    void emit_notifiesRegisteredListener() {
        List<Object> received = new ArrayList<>();
        bus.on("TEST_EVENT", received::add);
        bus.emit("TEST_EVENT", "hello");
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));
    }

    @Test
    void emit_multipleListeners_allNotified() {
        List<Object> a = new ArrayList<>();
        List<Object> b = new ArrayList<>();
        bus.on("EVT", a::add);
        bus.on("EVT", b::add);
        bus.emit("EVT", 42);
        assertEquals(1, a.size());
        assertEquals(1, b.size());
    }

    @Test
    void emit_differentEvent_doesNotNotifyOtherListeners() {
        List<Object> received = new ArrayList<>();
        bus.on("EVENT_A", received::add);
        bus.emit("EVENT_B", "data");
        assertTrue(received.isEmpty());
    }

    @Test
    void emit_noListeners_doesNotThrow() {
        assertDoesNotThrow(() -> bus.emit("UNKNOWN", "data"));
    }

    @Test
    void emit_nullPayload_deliveredAsNull() {
        List<Object> received = new ArrayList<>();
        bus.on("EVT", received::add);
        bus.emit("EVT", null);
        assertEquals(1, received.size());
        assertNull(received.get(0));
    }

    @Test
    void on_sameEventMultipleTimes_allCallbacksFire() {
        int[] count = {0};
        bus.on("EVT", data -> count[0]++);
        bus.on("EVT", data -> count[0]++);
        bus.on("EVT", data -> count[0]++);
        bus.emit("EVT", "x");
        assertEquals(3, count[0]);
    }

    @Test
    void emit_multipleEmits_listenerCalledEachTime() {
        int[] count = {0};
        bus.on("EVT", data -> count[0]++);
        bus.emit("EVT", "a");
        bus.emit("EVT", "b");
        bus.emit("EVT", "c");
        assertEquals(3, count[0]);
    }

    @Test
    void emit_passesCorrectDataType() {
        List<Object> received = new ArrayList<>();
        bus.on("EVT", received::add);
        String[] arr = {"user1", "hello"};
        bus.emit("EVT", arr);
        assertInstanceOf(String[].class, received.get(0));
        assertArrayEquals(arr, (String[]) received.get(0));
    }
}
