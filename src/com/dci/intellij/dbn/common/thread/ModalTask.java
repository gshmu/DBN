package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.routine.BackgroundRunnable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ModalTask<T> extends Task.Modal implements RunnableTask<T>{
    private T data;

    private ModalTask(Project project, String title, boolean cancellable) {
        super(project, title, cancellable);
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
        Failsafe.lenient(() -> {
            ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
            run(progressIndicator);
        });
    }

    @Override
    public final void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            Failsafe.lenient(() -> {
                progressIndicator.pushState();
                progressIndicator.setIndeterminate(true);
                execute(progressIndicator);
            });
        } finally {
            progressIndicator.popState();
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator);

    @Override
    public void start() {
        TaskUtil.startTask(this, getProject());
    }

    public static <T> void invoke(
            @NotNull Project project,
            String title,
            boolean cancellable,
            BackgroundRunnable.Unsafe<T> runnable) {
        create(project, title, cancellable, runnable).start();
    }

    @NotNull
    public static <T> ModalTask<T> create(@NotNull Project project, String title, boolean cancellable, BackgroundRunnable.Unsafe<T> runnable) {
        return new ModalTask<T>(project, title, cancellable) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                runnable.run(getData(), progressIndicator);
            }
        };
    }

}
