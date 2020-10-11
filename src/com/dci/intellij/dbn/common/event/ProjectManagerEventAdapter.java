package com.dci.intellij.dbn.common.event;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface ProjectManagerEventAdapter {
    default <T> void subscribe(Topic<T> topic, T handler) {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project project) {
                MessageBus messageBus = project.getMessageBus();
                MessageBusConnection connection = messageBus.connect();
                connection.subscribe(topic, handler);
            }
        });
    }
}
