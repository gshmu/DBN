package com.dci.intellij.dbn.common.thread;

public abstract class SimpleBackgroundTask extends SynchronizedTask{

    public SimpleBackgroundTask() {
        super(null);
    }

    public SimpleBackgroundTask(Object syncObject) {
        super(syncObject);
    }

    public final void start() {
        Thread thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
