package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ApplicationEvents {
    private ApplicationEvents() {
    }

    public static <T> void subscribe(@Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        Guarded.run(() -> {
            MessageBus messageBus = messageBus();
            MessageBusConnection connection = parentDisposable == null ?
                    messageBus.connect() :
                    messageBus.connect(Failsafe.nd(parentDisposable));
            connection.subscribe(topic, handler);
        });
    }

    public static <T> void notify(Topic<T> topic, ParametricRunnable.Basic<T> callback) {
        T publisher = publisher(topic);
        callback.run(publisher);
    }

    public static <T> T publisher(Topic<T> topic) {
        MessageBus messageBus = messageBus();
        return messageBus.syncPublisher(topic);
    }

    @NotNull
    private static MessageBus messageBus() {
        return ApplicationManager.getApplication().getMessageBus();
    }
}
