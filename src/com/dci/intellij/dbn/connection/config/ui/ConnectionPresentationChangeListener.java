package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import java.awt.Color;
import java.util.EventListener;

import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.util.messages.Topic;

public interface ConnectionPresentationChangeListener extends EventListener {
    Topic<ConnectionPresentationChangeListener> TOPIC = Topic.create("Connection presentation changed", ConnectionPresentationChangeListener.class);
    void presentationChanged(String name, Icon icon, Color color, ConnectionId connectionId, DatabaseType databaseType);
}
