package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public interface Background {
    Logger LOGGER = LoggerFactory.createLogger();

    static void run(ThrowableRunnable<Throwable> runnable) {
        try {
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
        } catch (RejectedExecutionException e) {
            LOGGER.warn("Background execution rejected: " + e.getMessage());
        }

    }

}
