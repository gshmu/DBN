package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public abstract class SimpleTimeoutCall<T> implements Callable<T>{
    public static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "DBN - Timed-out Execution Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    });

    private long timeout;
    private TimeUnit timeoutUnit;
    private T defaultValue;

    public SimpleTimeoutCall(long timeout, TimeUnit timeoutUnit, T defaultValue) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.defaultValue = defaultValue;
    }

    public final T start() {
        try {
            Future<T> future = POOL.submit(this);
            return future.get(timeout, timeoutUnit);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    protected T handleException(Exception e) {
        return defaultValue;
    }
}
