package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
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

    public BackgroundTask(@Nullable Project project, TaskInstructions instructions) {
        this(project, instructions.getTitle(), instructions.isStartInBackground(), instructions.isCanBeCancelled());
    }
    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean startInBackground, boolean canBeCancelled) {
        super(project, Constants.DBN_TITLE_PREFIX + title, canBeCancelled, startInBackground ? START_IN_BACKGROUND : DO_NOT_START_IN_BACKGROUND);
    }

    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean startInBackground) {
        this(project, title, startInBackground, false);
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
        } catch (ProcessCanceledException e) {
            // no action
        } catch (InterruptedException e) {
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

    protected static void initProgressIndicator(final ProgressIndicator progressIndicator, final boolean indeterminate, @Nullable final String text) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                if (progressIndicator.isRunning()) {
                    progressIndicator.setIndeterminate(indeterminate);
                    if (text != null) progressIndicator.setText(text);
                }
            }
        }.start();
    }

    public static boolean isProcessCancelled() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

}
