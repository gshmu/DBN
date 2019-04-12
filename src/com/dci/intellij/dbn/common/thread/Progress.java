package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface Progress {


    static void background(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        Failsafe.guarded(() -> {
            ThreadInfo invoker = ThreadMonitor.current();
            Task task = new Task.Backgroundable(Failsafe.nn(project), title, cancellable, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.PROGRESS,
                            () -> runnable.run(indicator));
                }
            };
            start(task);
        });
    }

    static void prompt(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        Failsafe.guarded(() -> {
            ThreadInfo invoker = ThreadMonitor.current();
            Task task = new Task.Backgroundable(Failsafe.nn(project), title, cancellable, PerformInBackgroundOption.DEAF) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.PROGRESS,
                            () -> runnable.run(indicator));
                }
            };
            start(task);
        });
    }

    static void modal(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        Failsafe.guarded(() -> {
            ThreadInfo invoker = ThreadMonitor.current();
            Task task = new Task.Modal(Failsafe.nn(project), title, cancellable) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.MODAL,
                            () -> runnable.run(indicator));

                }
            };
            start(task);
        });
    }

    static void start(Task task) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            if (Failsafe.check(task)) {
                ProgressManager progressManager = ProgressManager.getInstance();
                progressManager.run(task);
            }
        });
    }

    static void check(ProgressIndicator progress) {
        if (progress.isCanceled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }
}
