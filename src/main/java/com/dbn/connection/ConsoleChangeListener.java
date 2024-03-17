package com.dbn.connection;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConsoleChangeListener extends EventListener {
    Topic<ConsoleChangeListener> TOPIC = Topic.create("Connection console changed", ConsoleChangeListener.class);
    void consoleChanged(ConnectionId connectionId);
}
