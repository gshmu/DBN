package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.project.ProjectUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;

public interface ProjectManagerEventAdapter {
    default <T> void subscribe(Topic<T> topic, T handler) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            ProjectUtil.projectOpened(project -> {
                MessageBus messageBus = project.getMessageBus();
                MessageBusConnection connection = messageBus.connect();
                connection.subscribe(topic, handler);
            });
        });
    }
}
