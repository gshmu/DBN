package com.dci.intellij.dbn.common.event;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;

public class EventManager implements ApplicationComponent{
    private Map<Object, WeakReference<MessageBusConnection>> connectionCache = new HashMap<Object, WeakReference<MessageBusConnection>>();
    
    public static EventManager getInstance() {
        return ApplicationManager.getApplication().getComponent(EventManager.class);
    }

    private static MessageBusConnection connect(Object handler) {
        EventManager eventManager = EventManager.getInstance();
        WeakReference<MessageBusConnection> connectionRef = eventManager.connectionCache.get(handler);
        if (connectionRef == null || connectionRef.get() == null) {
            MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
            MessageBusConnection connect = messageBus.connect();
            connectionRef = new WeakReference<MessageBusConnection>(connect);
            //eventManager.connectionCache.put(handler, connectionRef);
        }
        return connectionRef.get();
    }


    private static MessageBusConnection connect(Project project, Object handler) {
        EventManager eventManager = EventManager.getInstance();
        WeakReference<MessageBusConnection> connection = eventManager.connectionCache.get(handler);
        if (connection == null) {
            MessageBus messageBus = project.getMessageBus();
            MessageBusConnection connect = messageBus.connect();
            connection = new WeakReference<MessageBusConnection>(connect);
            //eventManager.connectionCache.put(handler, connection);
        }
        return connection.get();
    }
    
    public static <T> void subscribe(Project project, Disposable parentDisposable, Topic<T> topic, final T handler) {
        if (project != null) {
            final MessageBusConnection messageBusConnection = connect(project, handler);
            messageBusConnection.subscribe(topic, handler);
            Disposer.register(parentDisposable, new Disposable() {
                @Override
                public void dispose() {
                    messageBusConnection.disconnect();
                    //unsubscribe(handler);
                }
            });
        }
    }

    public static <T> void subscribe(Disposable parentDisposable, Topic<T> topic, final T handler) {
        final MessageBusConnection messageBusConnection = connect(handler);
        messageBusConnection.subscribe(topic, handler);
        if (parentDisposable != null) {
            Disposer.register(parentDisposable, new Disposable() {
                @Override
                public void dispose() {
                    messageBusConnection.disconnect();
                    //unsubscribe(handler);
                }
            });
        }
    }

    @NotNull
    public static <T> T notify(@Nullable Project project, Topic<T> topic) {
        if (project == null || project.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        MessageBus messageBus = project.getMessageBus();
        return messageBus.syncPublisher(topic);
    }

    @Deprecated
    private static void unsubscribe(Object ... handlers) {
        EventManager eventManager = EventManager.getInstance();
        for (Object handler : handlers) {
            WeakReference<MessageBusConnection> connectionRef = eventManager.connectionCache.remove(handler);
            if (connectionRef != null) {
                MessageBusConnection connection = connectionRef.get();
                if (connection != null) {
                    connection.disconnect();
                }
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
        for (WeakReference<MessageBusConnection> connectionRef : connectionCache.values()) {
            MessageBusConnection connection = connectionRef.get();
            if (connection != null) {
                connection.disconnect();
            }
        }
        connectionCache.clear();
    }
}
