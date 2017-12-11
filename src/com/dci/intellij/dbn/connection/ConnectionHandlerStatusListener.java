package com.dci.intellij.dbn.connection;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionHandlerStatusListener extends EventListener {
    Topic<ConnectionHandlerStatusListener> TOPIC = Topic.create("Connection status changed", ConnectionHandlerStatusListener.class);
    void statusChanged(ConnectionId connectionId);
}
