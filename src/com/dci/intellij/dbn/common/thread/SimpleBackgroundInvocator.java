package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.ExecutorService;

public abstract class SimpleBackgroundInvocator extends SimpleTask{
    private SimpleBackgroundInvocator() {}

    public final void start() {
        ExecutorService executorService = ThreadFactory.backgroundExecutor();
        executorService.submit(this);
    }

    public static void invoke(Runnable runnable) {
        new SimpleBackgroundInvocator() {
            @Override
            protected void execute() {
                runnable.run();
            }
        }.start();
    }
}
