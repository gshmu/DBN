package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.ExecutorService;

public interface Background {
    static void run(Runnable runnable) {
        ExecutorService executorService = ThreadFactory.backgroundExecutor();
        executorService.submit(
                () -> ThreadMonitor.run(ThreadProperty.BACKGROUND_THREAD,
                        () -> runnable.run()));
    }

}
