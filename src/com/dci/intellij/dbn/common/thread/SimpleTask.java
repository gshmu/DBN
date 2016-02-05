package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SimpleTask<T> extends AbstractTask<T>{
    public SimpleTask() {
    }

    public SimpleTask(T executeOption) {
        super(executeOption);
    }

    public void start() {
        run();
    }

    protected boolean canExecute() {
        return getExecuteOption() == null || getExecuteOption().equals(getOption());
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
