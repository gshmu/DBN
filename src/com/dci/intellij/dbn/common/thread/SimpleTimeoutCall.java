package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.util.Traceable;

public abstract class SimpleTimeoutCall<T> extends Traceable implements Callable<T>{
    private long timeout;
    private TimeUnit timeoutUnit;
    private T defaultValue;
    private boolean daemon;

    public SimpleTimeoutCall(long timeout, TimeUnit timeoutUnit, T defaultValue, boolean daemon) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.defaultValue = defaultValue;
        this.daemon = daemon;
    }

    public final T start() {
        try {
            ExecutorService executorService = ThreadFactory.timeoutExecutor(daemon);
            Future<T> future = executorService.submit(this);
            try {
                return future.get(timeout, timeoutUnit);
            } catch (InterruptedException e) {
                future.cancel(true);
                return handleException(e);
            }

        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public abstract T call() throws Exception;

    protected T handleException(Exception e) {
        return defaultValue;
    }
}
