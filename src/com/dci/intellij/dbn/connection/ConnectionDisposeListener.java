package com.dci.intellij.dbn.connection;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionDisposeListener extends EventListener {
    Topic<ConnectionDisposeListener> TOPIC = Topic.create("Connection disposed", ConnectionDisposeListener.class);
    void connectionDisposing(String connectionId);
}
