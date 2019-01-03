package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
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
        try {
            ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            run(progressIndicator);
        } catch (ProcessCanceledException e) {
            // do nothing
        }
    }

    @Override
    public final void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            progressIndicator.pushState();
            progressIndicator.setIndeterminate(true);
            execute(progressIndicator);
        } catch (InterruptedException ignore){
        } catch (ProcessCanceledException ignore){
        } finally {
            progressIndicator.popState();
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException;

    public void start() {
        TaskUtil.startTask(this, getProject());
    }

    public static void invoke(
            @NotNull Project project,
            String title,
            boolean cancellable,
            BackgroundRunnable runnable) {
        create(project, title, cancellable, runnable).start();
    }

    @NotNull
    public static <T> ModalTask<T> create(@NotNull Project project, String title, boolean cancellable, BackgroundRunnable<T> runnable) {
        return new ModalTask<T>(project, title, cancellable) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                runnable.run(getData(), progressIndicator);
            }
        };
    }

}
