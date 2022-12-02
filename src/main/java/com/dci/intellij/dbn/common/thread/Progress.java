package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.util.Titles;
import com.dci.intellij.dbn.connection.context.DatabaseContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Checks.allValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.MODAL;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.PROGRESS;
import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static com.intellij.openapi.progress.PerformInBackgroundOption.DEAF;

public final class Progress {

    private Progress() {}

    public static void background(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadMonitor.current();
        Backgroundable task = new Backgroundable(project, title, cancellable, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.surround(invoker, PROGRESS, () -> guarded(() -> {
                    indicator.setText(text);
                    runnable.run(indicator);
                }));
            }
        };
        schedule(task);
    }


    public static void prompt(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadMonitor.current();
        Backgroundable task = new Backgroundable(project, title, cancellable, DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.surround(invoker, PROGRESS, () -> guarded(() -> {
                    indicator.setText(text);
                    runnable.run(indicator);
                }));
            }

            @Override
            public boolean isConditionalModal() {
                return true;
            }
        };
        schedule(task);
    }


    public static void modal(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadMonitor.current();
        Task.Modal task = new Task.Modal(project, title, cancellable) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ThreadMonitor.surround(invoker, MODAL, () -> guarded(() -> {
                    indicator.setText(text);
                    runnable.run(indicator);
                }));

            }
        };
        schedule(task);
    }

    private static void schedule(Task task) {
        if (!allValid(task, task.getProject())) return;

        ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.run(task);
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
