package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.Nullable;

public interface EventNotifier {
    static <T> void notify(@Nullable Project project, Topic<T> topic, ParametricRunnable.Basic<T> callback) {
        if (Failsafe.check(project) && /*!project.isDefault() &&*/ project != Failsafe.DUMMY_PROJECT) {
            try {
                MessageBus messageBus = project.getMessageBus();
                T publisher = messageBus.syncPublisher(topic);
                callback.run(publisher);
            } catch (ProcessCanceledException ignore) {}
        }
    }
}
