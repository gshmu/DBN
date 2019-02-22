package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.BackgroundRunnable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BackgroundTask<T> extends Task.Backgroundable implements RunnableTask<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private TaskInstructions instructions;
    private T data;

    private static PerformInBackgroundOption START_IN_BACKGROUND = new PerformInBackgroundOption() {
        @Override
        public boolean shouldStartInBackground() { return true;}
        @Override
        public void processSentToBackground() {}
    };

    private static PerformInBackgroundOption DO_NOT_START_IN_BACKGROUND = new PerformInBackgroundOption() {
        @Override
        public boolean shouldStartInBackground() { return false;}
        @Override
        public void processSentToBackground() {}
    };

    private BackgroundTask(@Nullable Project project, TaskInstructions instructions) {
        super(
            Failsafe.get(project),
            Constants.DBN_TITLE_PREFIX + instructions.getTitle(),
            instructions.is(TaskInstruction.CANCELLABLE),
            instructions.is(TaskInstruction.BACKGROUNDED) ?
                    START_IN_BACKGROUND :
                    DO_NOT_START_IN_BACKGROUND);
        this.instructions = instructions;
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
        ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
        run(progressIndicator);
    }

    @Override
    public final void run(@NotNull ProgressIndicator progressIndicator) {
        BackgroundMonitor.startBackgroundProcess();
        Thread currentThread = Thread.currentThread();
        int priority = currentThread.getPriority();
        try {
            progressIndicator.pushState();
            currentThread.setPriority(Thread.MIN_PRIORITY);
            initProgressIndicator(progressIndicator, true);

            execute(progressIndicator);
        } catch (ProcessCanceledException e) {
            // no action
        } catch (Exception e) {
            LOGGER.error("Error executing background operation.", e);
        } finally {
            currentThread.setPriority(priority);
            progressIndicator.popState();
            BackgroundMonitor.endBackgroundProcess();
            /*if (progressIndicator.isRunning()) {
                progressIndicator.stop();
            }*/
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator);

    @Override
    public final void start() {
        boolean conditional = instructions != null && instructions.is(TaskInstruction.CONDITIONAL);
        if (conditional && BackgroundMonitor.isBackgroundProcess()) {
            ProgressMonitor.checkCancelled();
            run(ProgressMonitor.getProgressIndicator());
        } else {
            TaskUtil.startTask(BackgroundTask.this, getProject());
        }
    }

    protected static void initProgressIndicator(ProgressIndicator progressIndicator, boolean indeterminate) {
        initProgressIndicator(progressIndicator, indeterminate, null);
    }

    public static void initProgressIndicator(ProgressIndicator progressIndicator, boolean indeterminate, @Nullable String text) {
        SimpleLaterInvocator.invoke(progressIndicator.getModalityState(), () -> {
            if (progressIndicator.isRunning()) {
                progressIndicator.setIndeterminate(indeterminate);
                if (text != null) progressIndicator.setText(text);
            }
        });
    }

    public static <T> BackgroundTask<T> create(@Nullable Project project, TaskInstructions instructions, BackgroundRunnable<T> runnable) {
        return new BackgroundTask<T>(project, instructions) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                runnable.run(getData(), progressIndicator);
            }
        };
    }
    public static <T> void invoke(@Nullable Project project, TaskInstructions instructions, BackgroundRunnable<T> runnable) {
        create(project, instructions, runnable).start();
    }
}
