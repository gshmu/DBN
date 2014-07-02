package com.dci.intellij.dbn.connection;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionManagerListener extends EventListener {
    Topic<ConnectionManagerListener> TOPIC = Topic.create("Connections changed", ConnectionManagerListener.class);
    void connectionsChanged();
}
