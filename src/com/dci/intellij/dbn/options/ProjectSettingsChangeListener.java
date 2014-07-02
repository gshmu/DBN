package com.dci.intellij.dbn.options;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ProjectSettingsChangeListener extends EventListener {
    Topic<ProjectSettingsChangeListener> TOPIC = Topic.create("Setting changes notification", ProjectSettingsChangeListener.class);
    void projectSettingsChanged(Project project);
}
