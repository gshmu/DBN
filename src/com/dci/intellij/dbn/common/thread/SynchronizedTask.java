package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SynchronizedTask extends SimpleTask {
    private final Object syncObject;

    public SynchronizedTask(Object syncObject) {
        this.syncObject = syncObject;
    }

    public void start() {
        run();
    }

    @Override
    public final void run() {
        try {
            if (canExecute()) {
                if (syncObject == null) {
                    execute();
                } else {
                    synchronized (syncObject) {
                        execute();
                    }
                }
            } else {
                cancel();
            }
        } catch (ProcessCanceledException ignore) {
        }
    }
}
