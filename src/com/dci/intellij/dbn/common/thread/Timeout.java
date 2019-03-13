package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Timeout {
    Logger LOGGER = LoggerFactory.createLogger();


    static <T> T call(long seconds, T defaultValue, boolean daemon, BasicCallable<T> callable) {
        try {
            ExecutorService executorService = ThreadFactory.timeoutExecutor(daemon);
            Future<T> future = executorService.submit(
                    () -> ThreadMonitor.call(ThreadProperty.TIMEOUT_PROCESS, defaultValue, callable));
            try {
                return future.get(seconds, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                future.cancel(true);
                return defaultValue;
            }
        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            LOGGER.error("Timeout operation failed. Returning default " + defaultValue, exception);
            return defaultValue;
        }
    }

    static void run(long seconds, boolean daemon, Runnable runnable) {
        try {
            ExecutorService executorService = ThreadFactory.timeoutExecutor(daemon);
            Future future = executorService.submit(
                    () -> ThreadMonitor.run(ThreadProperty.TIMEOUT_PROCESS, runnable));
            try {
                future.get(seconds, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                future.cancel(true);
            }

        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            LOGGER.error("Timeout operation failed.", exception);
        }
    }

}
