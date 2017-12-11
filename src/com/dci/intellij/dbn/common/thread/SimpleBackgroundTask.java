package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.ExecutorService;

public abstract class SimpleBackgroundTask extends SynchronizedTask{
    private String name;

    public SimpleBackgroundTask(String name) {
        this.name = name;
    }

    public final void start() {
        ExecutorService executorService = ThreadFactory.backgroundExecutor();
        executorService.submit(this);
    }

    @Override
    protected String getSyncKey() {
        return null;
    }
}
