package com.dci.intellij.dbn.common.event;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

public interface ApplicationEvents {

    static <T> void subscribe(@Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = parentDisposable == null ?
                messageBus.connect() :
                messageBus.connect(parentDisposable);
        connection.subscribe(topic, handler);
    }
}
