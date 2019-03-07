package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventUtil {
    @NotNull
    private static MessageBusConnection connect(@Nullable Disposable parentDisposable) {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        return parentDisposable == null ?
                messageBus.connect() :
                messageBus.connect(parentDisposable);
    }

    @NotNull
    private static MessageBusConnection connect(@NotNull Project project, @Nullable Disposable parentDisposable) {
        MessageBus messageBus = project.getMessageBus();
        return parentDisposable == null ?
                messageBus.connect(project) :
                messageBus.connect(parentDisposable);    }
    
    public static <T> void subscribe(Project project, @Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        if (Failsafe.check(project) && project != Failsafe.DUMMY_PROJECT) {
            MessageBusConnection messageBusConnection = connect(project, parentDisposable);
            messageBusConnection.subscribe(topic, handler);
        }
    }

    public static <T> void subscribe(@Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        MessageBusConnection messageBusConnection = connect(parentDisposable == null ? ApplicationManager.getApplication() : parentDisposable);
        messageBusConnection.subscribe(topic, handler);
    }

    public static <T> void notify(@Nullable Project project, Topic<T> topic, ParametricRunnable<T> callback) {
        if (Failsafe.check(project) && /*!project.isDefault() &&*/ project != Failsafe.DUMMY_PROJECT) {
            MessageBus messageBus = project.getMessageBus();
            T publisher = messageBus.syncPublisher(topic);
            Failsafe.lenient(() -> callback.run(publisher));
        }
    }
}
