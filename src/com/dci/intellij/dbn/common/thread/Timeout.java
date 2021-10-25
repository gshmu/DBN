package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
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

            return waitFor(future, seconds, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | RejectedExecutionException e) {
            String message = CommonUtil.nvl(e.getMessage(), e.getClass().getName());
            log.warn("Operation timed out. Returning default " + defaultValue + ". Cause: " + message);
        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            log.warn("Timeout operation failed. Returning default " + defaultValue, exception);
        }
        return defaultValue;
    }

    public static void run(long seconds, boolean daemon, ThrowableRunnable<Throwable> runnable) {
        try {
            ThreadInfo invoker = ThreadMonitor.current();
            ExecutorService executorService = ThreadPool.timeoutExecutor(daemon);
            Future<?> future = executorService.submit(
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
            waitFor(future, seconds, TimeUnit.SECONDS);

        } catch (TimeoutException | InterruptedException | RejectedExecutionException e) {
            String message = CommonUtil.nvl(e.getMessage(), e.getClass().getName());
            log.warn("Operation timed out. Cause: " + message);
        } catch (ExecutionException e) {
            Throwable exception = CommonUtil.nvl(e.getCause(), e);
            log.warn("Timeout operation failed.", exception);
        }
    }

    public static <T> T waitFor(Future<T> future, long time, TimeUnit timeUnit) throws InterruptedException, TimeoutException, ExecutionException {
        try {
            return future.get(time, timeUnit);
        } catch (TimeoutException | InterruptedException e) {
            future.cancel(true);
            throw e;
        }
    }

}
