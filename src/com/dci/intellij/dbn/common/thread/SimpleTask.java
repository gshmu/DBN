package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SimpleTask<T> extends AbstractTask<T>{
    public void start() {
        run();
    }

    protected boolean canExecute() {
        return true;
    }

    public void run() {
        try {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        } catch (ProcessCanceledException ignore) {
        }
    }

    protected abstract void execute();
}
