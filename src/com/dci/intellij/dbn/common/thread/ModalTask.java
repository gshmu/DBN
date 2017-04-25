package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

public abstract class ModalTask<T> extends Task.Modal implements RunnableTask<T>{
    private T option;

    public ModalTask(Project project, String title, boolean canBeCancelled) {
        super(project, title, canBeCancelled);
    }

    public ModalTask(Project project, String title) {
        super(project, title, false);
    }

    @Override
    public void setOption(T handle) {
        this.option = handle;
    }

    @Override
    public T getOption() {
        return option;
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
        } catch (ProcessCanceledException ignore){
        } finally {
            progressIndicator.popState();
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator);

    public void start() {
        TaskUtil.startTask(this, getProject());
    }
}
