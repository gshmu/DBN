package com.dci.intellij.dbn.common.thread;

public abstract class SimpleBackgroundTask implements Runnable{
    public final void start() {
        Thread thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
