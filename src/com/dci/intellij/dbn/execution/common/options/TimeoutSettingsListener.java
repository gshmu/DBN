package com.dci.intellij.dbn.execution.common.options;

import java.util.EventListener;

import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.intellij.util.messages.Topic;

public interface TimeoutSettingsListener extends EventListener {
    Topic<TimeoutSettingsListener> TOPIC = Topic.create("Timeout settings change event", TimeoutSettingsListener.class);
    void settingsChanged(ExecutionTarget executionTarget);
}
