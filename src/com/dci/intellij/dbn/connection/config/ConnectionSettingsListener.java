package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionSettingsListener extends EventListener {
    Topic<ConnectionSettingsListener> TOPIC = Topic.create("Connection changed", ConnectionSettingsListener.class);

    default void connectionsChanged() {}

    default void connectionChanged(ConnectionId connectionId) {}

    default void connectionNameChanged(ConnectionId connectionId) {}
}
