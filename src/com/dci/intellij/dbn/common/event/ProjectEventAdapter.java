package com.dci.intellij.dbn.common.event;

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

    interface Provided extends ProjectEventAdapter, Disposable {
        @NotNull Project getProject();

        default <T> void subscribe(@NotNull Topic<T> topic, @NotNull T handler) {
            subscribe(getProject(), this, topic, handler);
        }
    }
}
