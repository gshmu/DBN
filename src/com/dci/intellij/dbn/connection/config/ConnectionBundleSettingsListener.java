package com.dci.intellij.dbn.connection.config;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionBundleSettingsListener extends EventListener {
    Topic<ConnectionBundleSettingsListener> TOPIC = Topic.create("Connections changed", ConnectionBundleSettingsListener.class);
    void settingsChanged();
}
