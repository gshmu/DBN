package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ExecutorService;

public interface Background {
    Logger LOGGER = LoggerFactory.createLogger();

    static void run(ThrowableRunnable<Throwable> runnable) {
        ThreadInfo threadInfo = ThreadMonitor.current();
        ExecutorService executorService = ThreadPool.backgroundExecutor();
        executorService.submit(() -> {
            try {
                ThreadMonitor.run(
                        threadInfo,
                        ThreadProperty.BACKGROUND,
                        runnable);
            } catch (Throwable e) {
                LOGGER.error("Error executing background task", e);
            }
        });
    }

}
