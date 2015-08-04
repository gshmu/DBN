package com.dci.intellij.dbn.execution;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;

public abstract class ExecutionCancellableCall<T> implements Callable<T> {
    private static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "DBN - Execution Processor Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    });

    private static final Logger LOGGER = LoggerFactory.createLogger();

    private int timeout;
    private TimeUnit timeUnit;

    private transient ProgressIndicator progressIndicator;
    private transient Future<T> future;
    private Timer cancelCheckTimer;

    public ExecutionCancellableCall(int timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    }

    @Override
    public T call() throws Exception {
        return execute();
    }

    public abstract T execute() throws Exception;

    public final T start() throws SQLException {
        try {
            if (progressIndicator != null) {
                cancelCheckTimer = new Timer("DBN - Connection Pool Cleaner");
                TimerTask cancelCheckTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (progressIndicator != null && progressIndicator.isCanceled()) {
                            try {
                                ExecutionCancellableCall.this.cancel();
                            } catch (Exception e) {
                                LOGGER.warn("Error cancelling operation", e);
                            }
                            if (future != null) future.cancel(true);
                            cancelCheckTimer.cancel();
                        }
                    }
                };
                cancelCheckTimer.schedule(cancelCheckTask, TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
            }

            T result = null;
            try {
                future = POOL.submit(this);
                result = timeout == 0 ?  future.get() : future.get(timeout, timeUnit);
            } finally {
                progressIndicator = null;
                future = null;
                if (cancelCheckTimer != null) {
                    cancelCheckTimer.cancel();
                }
            }

            return result;
        } catch (CancellationException e) {
            throw new ProcessCanceledException();
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLTimeoutException) {
                handleTimeout();
            } else {
                handleException(cause);
            }

        } catch (TimeoutException e)  {
            handleTimeout();
        }
        return null;
    }

    public void handleTimeout() throws SQLTimeoutException {
        try {
            ExecutionCancellableCall.this.cancel();
        } catch (Exception ce) {
            LOGGER.warn("Error cancelling operation", ce);
        }
        throw new SQLTimeoutException("Operation has timed out (" + timeout + "s). Check timeout settings");
    }

    public void handleException(Throwable e) throws SQLException{
        if (e instanceof SQLException) {
            throw (SQLException) e;
        } else {
            throw new SQLException(e.getMessage(), e);
        }
    }

    public abstract void cancel() throws Exception;
}
