package com.dci.intellij.dbn.connection.config.ui;

import com.intellij.util.messages.Topic;

import javax.swing.Icon;
import java.awt.Color;
import java.util.EventListener;

public interface ConnectionPresentationChangeListener extends EventListener {
    Topic<ConnectionPresentationChangeListener> TOPIC = Topic.create("Connection presentation changed", ConnectionPresentationChangeListener.class);
    void presentationChanged(String name, Icon icon, Color color, String connectionId);
    void setConnectionId(String connectionId);
}
