package org.golfit.client.eventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {
    private static final EventBus INSTANCE = new EventBus();

    public static EventBus get() {
        return INSTANCE;
    }

    private final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();

    public void on(String event, Consumer<Object> consumer) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(consumer);
    }

    public void emit(String event, Object data) {
        List<Consumer<Object>> consumers = listeners.get(event);
        if(consumers != null) {
            consumers.forEach(consumer -> consumer.accept(data));
        }
    }
}
