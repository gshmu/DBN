package com.dci.intellij.dbn.connection.config;

import java.util.EventListener;

import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.util.messages.Topic;

public interface ConnectionSettingsListener extends EventListener {
    Topic<ConnectionSettingsListener> TOPIC = Topic.create("Connection changed", ConnectionSettingsListener.class);
    void connectionsChanged();
    void connectionChanged(ConnectionId connectionId);
    void connectionNameChanged(ConnectionId connectionId);
}
