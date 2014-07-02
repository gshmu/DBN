package com.dci.intellij.dbn.common.event;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EventManager implements ApplicationComponent{
    private Map<Object, MessageBusConnection> connectionCache = new HashMap<Object, MessageBusConnection>();
    
    public static EventManager getInstance() {
        return ApplicationManager.getApplication().getComponent(EventManager.class);
    }

    private static MessageBusConnection connect(Object handler) {
        EventManager eventManager = EventManager.getInstance();
        MessageBusConnection connection = eventManager.connectionCache.get(handler);
        if (connection == null) {
            MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
            connection = messageBus.connect();
            eventManager.connectionCache.put(handler, connection);
        }
        return connection;
    }


    private static MessageBusConnection connect(Project project, Object handler) {
        EventManager eventManager = EventManager.getInstance();
        MessageBusConnection connection = eventManager.connectionCache.get(handler);
        if (connection == null) {
            MessageBus messageBus = project.getMessageBus();
            connection = messageBus.connect();
            eventManager.connectionCache.put(handler, connection);
        }
        return connection;
    }
    
    public static <T> void subscribe(Project project, Topic<T> topic, T handler) {
        MessageBusConnection messageBusConnection = connect(project, handler);
        messageBusConnection.subscribe(topic, handler);
    }

    public static <T> void subscribe(Topic<T> topic, T handler) {
        MessageBusConnection messageBusConnection = connect(handler);
        messageBusConnection.subscribe(topic, handler);
    }

    public static <T> T notify(Project project, Topic<T> topic) {
        if (project != null) {
            MessageBus messageBus = project.getMessageBus();
            return messageBus.syncPublisher(topic);
        }
        return null;
    }

    public static void unsubscribe(Object ... handlers) {
        EventManager eventManager = EventManager.getInstance();
        for (Object handler : handlers) {
            MessageBusConnection connection = eventManager.connectionCache.remove(handler);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.EventManager";
    }
    

    public void dispose() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
        for (MessageBusConnection connection : connectionCache.values()) {
            connection.disconnect();
        }
        connectionCache.clear();
    }
}
