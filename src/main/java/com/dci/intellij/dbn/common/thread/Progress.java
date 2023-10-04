package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.Titles;
import com.dci.intellij.dbn.connection.context.DatabaseContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Checks.allValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.MODAL;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.PROGRESS;
import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static com.intellij.openapi.progress.PerformInBackgroundOption.DEAF;

@UtilityClass
public final class Progress {

    public static void background(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Backgroundable(project, title, cancellable, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute(indicator, PROGRESS, project, invoker, text, runnable);
            }
        });
    }


    public static void prompt(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Task.Backgroundable(project, title, cancellable, DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute(indicator, PROGRESS, project, invoker, text, runnable);
            }

            @Override
            public boolean shouldStartInBackground() {
                return false;
            }

            @Override
            public boolean isConditionalModal() {
                // TODO return true;
                return false;
            }
        });
    }


    public static void modal(Project project, DatabaseContext context, boolean cancellable, String title, String text, ProgressRunnable runnable) {
        if (isNotValid(project)) return;
        title = Titles.suffixed(title, context);

        ThreadInfo invoker = ThreadInfo.copy();
        schedule(new Task.Modal(project, title, cancellable) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute(indicator, MODAL, project, invoker, text, runnable);
            }
        });
    }

    private static void execute(ProgressIndicator indicator, ThreadProperty threadProperty, Project project, ThreadInfo invoker, String text, ProgressRunnable runnable) {
        ThreadMonitor.surround(project, invoker, threadProperty, () -> guarded(() -> {
            indicator.setText(text);
            runnable.run(indicator);
        }));
    }

    private static void schedule(Task task) {
        if (!allValid(task, task.getProject())) return;

        ProgressManager progressManager = ProgressManager.getInstance();
        Dispatch.run(() -> progressManager.run(task));
    }

    public static double progressOf(int is, int should) {
        return ((double) is) / should;
    }
}
