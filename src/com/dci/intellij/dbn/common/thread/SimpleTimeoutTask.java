package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SimpleTimeoutTask implements Runnable{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long timeoutSeconds;
    private boolean daemon;

    private SimpleTimeoutTask(long timeoutSeconds, boolean daemon) {
        this.timeoutSeconds = timeoutSeconds;
        this.daemon = daemon;
    }

    public final void start() {
        Future future = ThreadFactory.timeoutExecutor(daemon).submit(this);
        try {
            future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            future.cancel(true);
        } catch (Exception e) {
            LOGGER.warn("Failed to execute timeout task", e);
        }
    }

    public static SimpleTimeoutTask create(long timeoutSeconds, boolean daemon, Runnable runnable) {
        return new SimpleTimeoutTask(timeoutSeconds, daemon) {
            @Override
            public void run() {
                try {
                    BackgroundMonitor.startTimeoutProcess();
                    runnable.run();
                } catch (ProcessCanceledException ignore){
                } finally {
                    BackgroundMonitor.endTimeoutProcess();
                }
            }
        };
    }

    public static void invoke(long timeoutSeconds, boolean daemon, Runnable runnable) {
        SimpleTimeoutTask.create(timeoutSeconds, daemon, runnable).start();
    }
}
