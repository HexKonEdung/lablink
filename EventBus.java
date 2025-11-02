package Main;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Very small application EventBus for UI refresh events. Usage:
 * EventBus.addListener("test.saved", (type, payload) -> { ... });
 * EventBus.post("test.saved", testId);
 */
public class EventBus {

    private static final Map<String, CopyOnWriteArrayList<BiConsumer<String, Object>>> listeners = new ConcurrentHashMap<>();

    public static void addListener(String eventType, BiConsumer<String, Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void removeListener(String eventType, BiConsumer<String, Object> listener) {
        CopyOnWriteArrayList<BiConsumer<String, Object>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public static void post(String eventType, Object payload) {
        CopyOnWriteArrayList<BiConsumer<String, Object>> list = listeners.get(eventType);
        if (list == null) {
            return;
        }
        for (BiConsumer<String, Object> l : list) {
            try {
                l.accept(eventType, payload);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
