package com.dci.intellij.dbn.common.thread;

public abstract class AbstractTask<T> implements RunnableTask<T>{
    private T data;

    private boolean cancelled;

    public AbstractTask() {
    }

    @Override
    public final T getData() {
        return data;
    }

    @Override
    public final void setData(T data) {
        this.data = data;
    }

    protected void cancel() {
        cancelled = true;
    }

    protected boolean isCancelled() {
        return cancelled;
    }
}
