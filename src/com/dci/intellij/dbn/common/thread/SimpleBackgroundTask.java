package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.ExecutorService;

public abstract class SimpleBackgroundTask extends SimpleTask{
    private SimpleBackgroundTask() {}

    @Override
    public final void start() {
        ExecutorService executorService = ThreadFactory.backgroundExecutor();
        executorService.submit(this);
    }

    public static void invoke(Runnable runnable) {
        new SimpleBackgroundTask() {
            @Override
            protected void execute() {
                BackgroundMonitor.run(ThreadProperty.BACKGROUND_PROCESS, () -> runnable.run());
            }
        }.start();
    }
}
