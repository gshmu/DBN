package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BackgroundTask<T> extends Task.Backgroundable implements RunnableTask<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private T data;

    private static PerformInBackgroundOption START_IN_BACKGROUND = new PerformInBackgroundOption() {
        public boolean shouldStartInBackground() { return true;}
        public void processSentToBackground() {}
    };

    private static PerformInBackgroundOption DO_NOT_START_IN_BACKGROUND = new PerformInBackgroundOption() {
        public boolean shouldStartInBackground() { return false;}
        public void processSentToBackground() {}
    };

    private BackgroundTask(@Nullable Project project, TaskInstructions instructions) {
        this(project, instructions.getTitle(), instructions.isStartInBackground(), instructions.isCanBeCancelled());
    }

    private BackgroundTask(@Nullable Project project, @NotNull String title, boolean startInBackground, boolean canBeCancelled) {
        super(
            FailsafeUtil.get(project),
            Constants.DBN_TITLE_PREFIX + title,
            canBeCancelled,
            startInBackground ? START_IN_BACKGROUND : DO_NOT_START_IN_BACKGROUND);
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public final void run() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        run(progressIndicator);
    }

    public final void run(@NotNull ProgressIndicator progressIndicator) {
        Thread currentThread = Thread.currentThread();
        int priority = currentThread.getPriority();
        try {
            progressIndicator.pushState();
            currentThread.setPriority(Thread.MIN_PRIORITY);
            initProgressIndicator(progressIndicator, true);

            execute(progressIndicator);
        } catch (ProcessCanceledException | InterruptedException e) {
            // no action
        } catch (Exception e) {
            LOGGER.error("Error executing background operation.", e);
        } finally {
            currentThread.setPriority(priority);
            progressIndicator.popState();
            /*if (progressIndicator.isRunning()) {
                progressIndicator.stop();
            }*/
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException;

    public final void start() {
        final BackgroundTask task = BackgroundTask.this;
        TaskUtil.startTask(task, getProject());
    }

    protected static void initProgressIndicator(final ProgressIndicator progressIndicator, final boolean indeterminate) {
        initProgressIndicator(progressIndicator, indeterminate, null);
    }

    public static void initProgressIndicator(final ProgressIndicator progressIndicator, final boolean indeterminate, @Nullable final String text) {
        SimpleLaterInvocator.invoke(() -> {
            if (progressIndicator.isRunning()) {
                progressIndicator.setIndeterminate(indeterminate);
                if (text != null) progressIndicator.setText(text);
            }
        });
    }

    public static boolean isProcessCancelled() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

    public static <T> BackgroundTask<T> create(@Nullable Project project, TaskInstructions instructions, BackgroundRunnable<T> runnable) {
        return new BackgroundTask<T>(project, instructions) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                runnable.run(this, progressIndicator);
            }
        };
    }
    public static <T> BackgroundTask<T> create(@Nullable Project project, @NotNull String title, boolean startInBackground, boolean cancellable, BackgroundRunnable<T> runnable) {
        return new BackgroundTask<T>(project, title, startInBackground, cancellable) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                runnable.run(this, progressIndicator);
            }
        };
    }

    public static <T> void invoke(@Nullable Project project, TaskInstructions instructions, BackgroundRunnable<T> runnable) {
        create(project, instructions, runnable).start();
    }

    public static <T> void invoke(@Nullable Project project, String title, boolean startInBackground, boolean cancellable, BackgroundRunnable<T> runnable) {
        create(project, title, startInBackground, cancellable, runnable).start();
    }

    @FunctionalInterface
    public interface BackgroundRunnable<T> {
        void run(BackgroundTask<T> task, ProgressIndicator progress) throws InterruptedException;
    }
}
