package com.dci.intellij.dbn.common.event;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.project.ProjectUtil;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;


public interface ProjectEvents {
    static <T> void subscribe(@NotNull Project project, @Nullable Disposable parentDisposable, Topic<T> topic, T handler) {
        MessageBus messageBus = project.getMessageBus();
        MessageBusConnection connection = parentDisposable == null ?
                messageBus.connect() :
                messageBus.connect(parentDisposable);

        connection.subscribe(topic, handler);
    }

    static <T> void subscribe(Topic<T> topic, T handler) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Arrays.stream(openProjects).forEach(project -> subscribe(project, null, topic, handler));

        Application application = ApplicationManager.getApplication();
        application.invokeLater(() ->
                ProjectUtil.projectOpened(project ->
                        subscribe(project, null, topic, handler)));

    }

    static <T> void notify(@Nullable Project project, Topic<T> topic, ParametricRunnable.Basic<T> callback) {
        if (Failsafe.check(project) && project != Failsafe.DUMMY_PROJECT) {
            try {
                MessageBus messageBus = Failsafe.nd(project).getMessageBus();
                T publisher = messageBus.syncPublisher(topic);
                callback.run(publisher);
            } catch (ProcessCanceledException ignore) {}
        }
    }
}
