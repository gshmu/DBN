package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

@Deprecated
public abstract class SimpleTask<T> extends AbstractTask<T>{
    protected SimpleTask() {
    }

    @Override
    public void start() {
        run();
    }

    protected boolean canExecute() {
        return true;
    }

    @Override
    public void run() {
        trace(this);
        try {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        } catch (ProcessCanceledException ignore) {}
    }

    protected abstract void execute();
}
