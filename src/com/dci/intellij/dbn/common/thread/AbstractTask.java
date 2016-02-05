package com.dci.intellij.dbn.common.thread;

public abstract class AbstractTask<T> implements RunnableTask<T>{
    private T option;
    private T executeOption;

    private boolean cancelled;

    public AbstractTask() {
    }

    public AbstractTask(T executeOption) {
        this.executeOption = executeOption;
    }

    public T getExecuteOption() {
        return executeOption;
    }

    @Override
    public final T getOption() {
        return option;
    }

    @Override
    public final void setOption(T option) {
        this.option = option;
    }

    protected void cancel() {
        cancelled = true;
    }

    protected boolean isCancelled() {
        return cancelled;
    }
}
