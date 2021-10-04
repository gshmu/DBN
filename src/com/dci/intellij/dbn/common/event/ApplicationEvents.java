package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

public final class ApplicationEvents {
    private ApplicationEvents() {}

    public static <T> void subscribe(@Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        try {
            MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
            MessageBusConnection connection = parentDisposable == null ?
                    messageBus.connect() :
                    messageBus.connect(Failsafe.nd(parentDisposable));
            connection.subscribe(topic, handler);
        } catch (ProcessCanceledException ignore) {}
    }
}
