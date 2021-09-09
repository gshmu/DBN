package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public final class Timeout {
    private Timeout() {}

    public static <T> T call(long seconds, T defaultValue, boolean daemon, ThrowableCallable<T, Throwable> callable) {
        try {
            ThreadInfo invoker = ThreadMonitor.current();
            ExecutorService executorService = ThreadPool.timeoutExecutor(daemon);
            Future<T> future = executorService.submit(
                    () -> {
                        try {
                            return ThreadMonitor.call(
                                    invoker,
                                    ThreadProperty.TIMEOUT,
                                    defaultValue,
                                    callable);
                        } catch (Throwable e) {
                            log.error("Timeout operation failed. Returning default " + defaultValue, e);
                            return defaultValue;

                        }
                    });
            try {
                return future.get(seconds, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                future.cancel(true);
                return defaultValue;
            }
        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            log.error("Timeout operation failed. Returning default " + defaultValue, exception);
            return defaultValue;
        }
    }

    public static void run(long seconds, boolean daemon, ThrowableRunnable<Throwable> runnable) {
        try {
            ThreadInfo invoker = ThreadMonitor.current();
            ExecutorService executorService = ThreadPool.timeoutExecutor(daemon);
            Future future = executorService.submit(
                    () -> {
                        try {
                            ThreadMonitor.run(
                                    invoker,
                                    ThreadProperty.TIMEOUT,
                                    runnable);
                        } catch (Throwable e) {
                            log.error("Timeout operation failed.", e);
                        }
                    });
            try {
                future.get(seconds, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                future.cancel(true);
            }

        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            log.error("Timeout operation failed.", exception);
        }
    }

}
