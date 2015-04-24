package com.dci.intellij.dbn.connection.config;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionSetupListener extends EventListener {
    Topic<ConnectionSetupListener> TOPIC = Topic.create("Connections changed", ConnectionSetupListener.class);
    void setupChanged();
}
