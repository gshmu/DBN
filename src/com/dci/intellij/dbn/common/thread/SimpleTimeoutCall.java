package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class SimpleTimeoutCall<T> implements Callable<T>{
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
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<T> future = executor.submit(this);
            return future.get(timeout, timeoutUnit);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    protected T handleException(Exception e) {
        return defaultValue;
    }
}
