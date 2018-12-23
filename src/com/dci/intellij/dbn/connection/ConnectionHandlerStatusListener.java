package com.dci.intellij.dbn.connection;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionHandlerStatusListener extends EventListener {
    Topic<ConnectionHandlerStatusListener> TOPIC = Topic.create("Connection status changed", ConnectionHandlerStatusListener.class);
    void statusChanged(ConnectionId connectionId, SessionId sessionId);
}
