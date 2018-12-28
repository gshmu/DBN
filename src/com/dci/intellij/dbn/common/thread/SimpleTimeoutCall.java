package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.Traceable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SimpleTimeoutCall<T> extends Traceable implements Callable<T>{
    private long timeoutSeconds;
    private T defaultValue;
    private boolean daemon;

    public SimpleTimeoutCall(long timeoutSeconds, T defaultValue, boolean daemon) {
        this.timeoutSeconds = timeoutSeconds;
        this.defaultValue = defaultValue;
        this.daemon = daemon;
    }

    public final T start() {
        try {
            ExecutorService executorService = ThreadFactory.timeoutExecutor(daemon);
            Future<T> future = executorService.submit(this);
            try {
                return future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException e) {
                future.cancel(true);
                return defaultValue;
            }

        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public abstract T call() throws Exception;

    public static <T> T invoke(long timeoutSeconds, T defaultValue, boolean daemon, Callable<T> callable) {
        return new SimpleTimeoutCall<T>(timeoutSeconds, defaultValue, daemon) {
            @Override
            public T call() throws Exception {
                return callable.call();
            }
        }.start();
    }
}
