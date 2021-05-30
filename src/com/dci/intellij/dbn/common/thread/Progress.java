package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.dci.intellij.dbn.common.util.Safe;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class Progress {

    private Progress() {}

    public static void background(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Task.Backgroundable(Failsafe.nd(project), title, cancellable, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.PROGRESS,
                            () -> Safe.run(() -> runnable.run(indicator)));
                }
            });
        }
    }

    public static void prompt(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Task.Backgroundable(Failsafe.nd(project), title, cancellable, PerformInBackgroundOption.DEAF) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.PROGRESS,
                            () -> Safe.run(() -> runnable.run(indicator)));
                }
            });
        }
    }

    public static void modal(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Task.Modal(project, title, cancellable) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.MODAL,
                            () -> Safe.run(() -> runnable.run(indicator)));

                }
            });
        }
    }

    public static void start(Task task) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            if (Failsafe.check(task) && Failsafe.check(task.getProject())) {
                ProgressManager progressManager = ProgressManager.getInstance();
                progressManager.run(task);
            }
        });
    }

    public static void check(ProgressIndicator progress) {
        if (progress.isCanceled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }
}
