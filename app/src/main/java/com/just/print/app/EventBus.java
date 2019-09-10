package com.just.print.app;


import com.just.print.util.L;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangx on 2016/11/2.
 */
public interface EventBus {
    public class EventBusImpl implements EventBus {
        static final String tag = "EventBusImpl";
        final Map<String, EventHandler> event = new HashMap<String, EventHandler>();

        public void register(String eventName, EventBus.EventHandler handler) {
            event.put(eventName, handler);
        }

        public void post(String eventName, Object... argument) {
            EventBus.EventHandler handler = event.get(eventName);
            if (handler != null)
                handler.handleEvent(eventName, argument);
            else {
                L.d(tag, "no search eventName" + eventName);
            }
        }

        public void unregister(String eventName) {
            event.remove(eventName);
        }
    }

    void post(String eventName, Object... argument);

    void register(String eventName, EventBus.EventHandler handler);

    void unregister(String eventName);

    public interface EventHandler {
        void handleEvent(String eventName, Object... argument);
    }
}
