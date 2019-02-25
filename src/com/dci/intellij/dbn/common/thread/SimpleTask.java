package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;

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
        Failsafe.lenient(() -> {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        });
    }

    protected abstract void execute();
}
