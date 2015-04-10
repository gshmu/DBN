package com.dci.intellij.dbn.common.locale.options;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface RegionalSettingsListener extends EventListener {
    Topic<RegionalSettingsListener> TOPIC = Topic.create("regional settings change event", RegionalSettingsListener.class);
    void settingsChanged();
}
