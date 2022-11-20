package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Checks;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.dci.intellij.dbn.common.util.Cancellable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.MODAL;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.PROGRESS;
import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static com.intellij.openapi.progress.PerformInBackgroundOption.DEAF;

public final class Progress {

    private Progress() {}

    public static void background(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (isNotValid(project)) return;

        ThreadInfo invoker = ThreadMonitor.current();
        Backgroundable task = new Backgroundable(Failsafe.nd(project), title, cancellable, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.run(
                        invoker,
                        PROGRESS,
                        () -> Cancellable.run(() -> runnable.run(indicator)));
            }
        };
        start(task);
    }

    public static void prompt(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (isNotValid(project)) return;

        ThreadInfo invoker = ThreadMonitor.current();
        Backgroundable task = new Backgroundable(Failsafe.nd(project), title, cancellable, DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.run(
                        invoker,
                        PROGRESS,
                        () -> Cancellable.run(() -> runnable.run(indicator)));
            }
        };
        start(task);
    }

    public static void modal(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (isNotValid(project)) return;

        ThreadInfo invoker = ThreadMonitor.current();
        Task.Modal task = new Task.Modal(project, title, cancellable) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.run(
                        invoker,
                        MODAL,
                        () -> Cancellable.run(() -> runnable.run(indicator)));

            }
        };
        start(task);
    }

    private static void start(Task task) {
        if (Checks.allValid(task, task.getProject())) {
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.run(task);
        }
    }

    public static void check(ProgressIndicator progress) {
        if (progress.isCanceled()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public static double progressOf(int is, int should) {
        return ((double) is) / should;
    }
}
