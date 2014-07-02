package com.dci.intellij.dbn.browser.options;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ObjectDisplaySettingsListener extends EventListener {
    Topic<ObjectDisplaySettingsListener> TOPIC = Topic.create("DBNavigator - Object display settings changed", ObjectDisplaySettingsListener.class);
    void displayDetailsChanged();
}
