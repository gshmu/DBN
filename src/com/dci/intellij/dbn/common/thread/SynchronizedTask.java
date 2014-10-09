package com.dci.intellij.dbn.common.thread;

public abstract class SynchronizedTask extends RunnableTask {
    private final Object syncObject;

    public SynchronizedTask(Object syncObject) {
        this.syncObject = syncObject;
    }

    public void start() {
        run();
    }

    @Override
    public final void run() {
        if (syncObject == null) {
            execute();
        } else {
            synchronized (syncObject) {
                execute();
            }
        }
    }
    protected abstract void execute();
}
