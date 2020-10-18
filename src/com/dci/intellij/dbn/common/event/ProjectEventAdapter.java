package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface ProjectEventAdapter {
    default <T> void subscribe(@NotNull Project project, @NotNull Disposable parentDisposable, @NotNull Topic<T> topic, @NotNull T handler) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connection = messageBus.connect(parentDisposable);
        connection.subscribe(topic, handler);
    }

    interface Provided extends ProjectEventAdapter, ProjectSupplier, Disposable {
        default <T> void subscribe(@NotNull Topic<T> topic, @NotNull T handler) {
            Project project = getProject();
            if (project != null) {
                subscribe(project, this, topic, handler);
            } else {
                throw new IllegalStateException("Project not provided");
            }

        }
    }
}
