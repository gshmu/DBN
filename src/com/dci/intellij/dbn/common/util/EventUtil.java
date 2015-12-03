package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;

public class EventUtil {
    private static MessageBusConnection connect() {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        return messageBus.connect();
    }

    private static MessageBusConnection connect(Project project) {
        MessageBus messageBus = project.getMessageBus();
        return messageBus.connect();
    }
    
    public static <T> void subscribe(Project project, Disposable parentDisposable, Topic<T> topic, final T handler) {
        if (project != null && project != FailsafeUtil.DUMMY_PROJECT) {
            final MessageBusConnection messageBusConnection = connect(project);
            messageBusConnection.subscribe(topic, handler);
            Disposer.register(parentDisposable, new Disposable() {
                @Override
                public void dispose() {
                    messageBusConnection.disconnect();
                }
            });
        }
    }

    public static <T> void subscribe(Disposable parentDisposable, Topic<T> topic, final T handler) {
        final MessageBusConnection messageBusConnection = connect();
        messageBusConnection.subscribe(topic, handler);
        if (parentDisposable == null) {
            parentDisposable = ApplicationManager.getApplication();
        }
        Disposer.register(parentDisposable, new Disposable() {
            @Override
            public void dispose() {
                messageBusConnection.disconnect();
            }
        });
    }

    @NotNull
    public static <T> T notify(@Nullable Project project, Topic<T> topic) {
        if (project == null || project.isDisposed() || project == FailsafeUtil.DUMMY_PROJECT) {
            throw AlreadyDisposedException.INSTANCE;
        }
        MessageBus messageBus = project.getMessageBus();
        return messageBus.syncPublisher(topic);
    }
}
