package com.dci.intellij.dbn.connection;

import java.util.EventListener;
import java.util.List;

import com.intellij.util.messages.Topic;

public interface ConnectionDisposeListener extends EventListener {
    Topic<ConnectionDisposeListener> TOPIC = Topic.create("Connection disposed", ConnectionDisposeListener.class);
    void connectionsDisposing(List<String> connectionId);
}
