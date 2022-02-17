package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ProgressRunnable;
import com.dci.intellij.dbn.common.util.Safe;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static com.intellij.openapi.progress.PerformInBackgroundOption.DEAF;

public final class Progress {

    private Progress() {}

    public static void background(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Backgroundable(Failsafe.nd(project), title, cancellable, ALWAYS_BACKGROUND) {
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

    public static void background(Project project, String title, AtomicReference<Thread> handle, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            Thread current = handle.get();
            if (current != null) {
                current.interrupt();
            }
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Backgroundable(Failsafe.nd(project), title, false, ALWAYS_BACKGROUND) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    ThreadMonitor.run(
                            invoker,
                            ThreadProperty.PROGRESS,
                            () -> Safe.run(() -> {
                                try {
                                    handle.set(Thread.currentThread());
                                    runnable.run(indicator);
                                } finally {
                                    handle.set(null);
                                }

                            }));
                }
            });
        }
    }

    public static void prompt(Project project, String title, boolean cancellable, ProgressRunnable runnable) {
        if (Failsafe.check(project)) {
            ThreadInfo invoker = ThreadMonitor.current();
            start(new Backgroundable(Failsafe.nd(project), title, cancellable, DEAF) {
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

    private static void start(Task task) {
        if (Failsafe.check(task) && Failsafe.check(task.getProject())) {
            Dispatch.runConditional(() -> {
                ProgressManager progressManager = ProgressManager.getInstance();
                progressManager.run(task);
            });
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
