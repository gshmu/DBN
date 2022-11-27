package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionConfigListener extends EventListener {
    Topic<ConnectionConfigListener> TOPIC = Topic.create("Connection changed", ConnectionConfigListener.class);

    default void connectionsChanged() {}

    default void connectionRemoved(ConnectionId connectionId) {}

    default void connectionChanged(ConnectionId connectionId) {}

    default void connectionNameChanged(ConnectionId connectionId) {}

    static ConnectionConfigAdapter whenSetupChanged(Runnable consumer) {
        return new ConnectionConfigAdapter().whenSetupChanged(consumer);
    }

    static ConnectionConfigAdapter whenChanged(Consumer<ConnectionId> consumer) {
        return new ConnectionConfigAdapter().whenChanged(consumer);
    }

    static ConnectionConfigAdapter whenRemoved(Consumer<ConnectionId> consumer) {
        return new ConnectionConfigAdapter().whenRemoved(consumer);
    }

    static ConnectionConfigAdapter whenChangedOrRemoved(Consumer<ConnectionId> consumer) {
        return new ConnectionConfigAdapter().whenChanged(consumer).whenRemoved(consumer);
    }

    static ConnectionConfigAdapter whenNameChanged(Consumer<ConnectionId> consumer) {
        return new ConnectionConfigAdapter().whenNameChanged(consumer);
    }

}
