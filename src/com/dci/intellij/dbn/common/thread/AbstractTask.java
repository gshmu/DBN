package com.dci.intellij.dbn.common.thread;

public abstract class AbstractTask<T> implements RunnableTask<T>{
    private T option;
    private boolean cancelled;

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
