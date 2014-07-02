package com.dci.intellij.dbn.common.environment;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface EnvironmentChangeListener extends EventListener {
    Topic<EnvironmentChangeListener> TOPIC = Topic.create("Environment changed", EnvironmentChangeListener.class);
    void environmentConfigChanged(String environmentTypeId);
    void environmentVisibilitySettingsChanged();
}
