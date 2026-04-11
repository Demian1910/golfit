package org.golfit.client.eventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple publish-subscribe event bus for decoupling UI components from network handlers.
 * Uses a singleton pattern so all components share the same event bus instance.
 */
public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    /** Returns the singleton EventBus instance. */
    public static EventBus get() {
        return INSTANCE;
    }

    private final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();

    /**
     * Registers a listener for the given event type.
     *
     * @param event    the event name to listen for
     * @param consumer the callback invoked when the event is emitted
     */
    public void on(String event, Consumer<Object> consumer) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(consumer);
    }

    /**
     * Emits an event to all registered listeners.
     *
     * @param event the event name
     * @param data  the data payload passed to each listener
     */
    public void emit(String event, Object data) {
        List<Consumer<Object>> consumers = listeners.get(event);
        if(consumers != null) {
            consumers.forEach(consumer -> consumer.accept(data));
        }
    }
}
