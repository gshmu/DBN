package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.Traceable;

@Deprecated
public abstract class AbstractTask<T> extends Traceable implements RunnableTask<T>{
    private T data;

    private boolean cancelled;

    protected AbstractTask() {}

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

    public boolean isCancelled() {
        return cancelled;
    }
}
