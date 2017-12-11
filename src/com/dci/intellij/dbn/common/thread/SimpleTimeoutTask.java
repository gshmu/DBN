package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;

public abstract class SimpleTimeoutTask implements Runnable{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long timeout;
    private TimeUnit timeoutUnit;
    private boolean daemon;

    protected SimpleTimeoutTask(long timeout, TimeUnit timeoutUnit, boolean daemon) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.daemon = daemon;
    }

    public final void start() {
        Future future = ThreadFactory.timeoutExecutor(daemon).submit(this);
        try {
            future.get(timeout, timeoutUnit);
        } catch (TimeoutException ignore) {
        } catch (Exception e) {
            LOGGER.warn("Failed to execute timeout task", e);
        }
    }
}
