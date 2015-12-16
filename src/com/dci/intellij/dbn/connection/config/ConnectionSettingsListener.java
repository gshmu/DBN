package com.dci.intellij.dbn.connection.config;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionSettingsListener extends EventListener {
    Topic<ConnectionSettingsListener> TOPIC = Topic.create("Connection changed", ConnectionSettingsListener.class);
    void connectionsChanged();
    void connectionChanged(String connectionId);
    void connectionNameChanged(String connectionId);
}
