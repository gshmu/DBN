package com.dci.intellij.dbn.common.thread;

public abstract class SimpleTimeoutTask implements Runnable{
    private String name;
    private long timeout;

    public SimpleTimeoutTask(String name, long timeoutMillis) {
        this.name = name;
        this.timeout = timeoutMillis;
    }

    public void start() throws Exception {
        Thread thread = new Thread(this, name);
        thread.start();
        thread.join(timeout);
    }
}
