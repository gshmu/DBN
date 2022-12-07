package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.util.Commons;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.dci.intellij.dbn.common.exception.Exceptions.causeOf;
import static com.dci.intellij.dbn.common.util.TimeUtil.millisSince;
import static com.dci.intellij.dbn.common.util.TimeUtil.secondsSince;

@Slf4j
public final class Timeout {
    private static final Object lock = new Object();

    private Timeout() {}

    public static <T> T call(long seconds, T defaultValue, boolean daemon, ThrowableCallable<T, Throwable> callable) {
        long start = System.currentTimeMillis();
        try {
            Threads.delay(lock);
            ThreadInfo invoker = ThreadMonitor.current();
            ExecutorService executorService = Threads.timeoutExecutor(daemon);
            Future<T> future = executorService.submit(
                    () -> {
                        try {
                            return ThreadMonitor.surround(
                                    invoker.getProject(),
                                    invoker,
                                    ThreadProperty.TIMEOUT,
                                    defaultValue,
                                    callable);
                        } catch (Throwable e) {
                            log.error("Timeout operation. Returning default {}", defaultValue, e);
                            return defaultValue;

                        }
                    });

            return waitFor(future, seconds, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | RejectedExecutionException e) {
            String message = Commons.nvl(e.getMessage(), e.getClass().getSimpleName());
            log.warn("Operation timed out after {} millis (timeout = {} seconds). Returning default {}. Cause: {}", secondsSince(start), seconds, defaultValue, message);
        } catch (ExecutionException e) {
            log.warn("Operation failed after {} millis (timeout = {} seconds). Returning default {}", millisSince(start), seconds, defaultValue, causeOf(e));
        }
        return defaultValue;
    }

    public static void run(long seconds, boolean daemon, ThrowableRunnable<Throwable> runnable) {
        long start = System.currentTimeMillis();
        try {
            Threads.delay(lock);
            ThreadInfo invoker = ThreadMonitor.current();
            ExecutorService executorService = Threads.timeoutExecutor(daemon);
            Future<?> future = executorService.submit(
                    () -> {
                        try {
                            ThreadMonitor.surround(
                                    invoker.getProject(),
                                    invoker,
                                    ThreadProperty.TIMEOUT,
                                    runnable);
                        } catch (Throwable e) {
                            log.error("Timeout operation failed.", e);
                        }
                    });
            waitFor(future, seconds, TimeUnit.SECONDS);

        } catch (TimeoutException | InterruptedException | RejectedExecutionException e) {
            String message = Commons.nvl(e.getMessage(), e.getClass().getSimpleName());
            log.warn("Operation timed out after {} millis (timeout = {} seconds). Cause: {}", millisSince(start), seconds, message);
        } catch (ExecutionException e) {
            log.warn("Operation failed after {} millis (timeout = {} seconds)", millisSince(start), seconds, causeOf(e));
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
