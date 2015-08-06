package com.dci.intellij.dbn.database.common.statement;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;

public abstract class StatementExecutor<T> implements Callable<T>{
    public static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "DBN - Database Interface Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    });

    private long timeoutSeconds;

    public StatementExecutor(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public final T call() throws Exception {
        Thread currentThread = Thread.currentThread();
        int initialPriority = currentThread.getPriority();
        currentThread.setPriority(Thread.MIN_PRIORITY);
        try {
            return execute();
        } finally {
            currentThread.setPriority(initialPriority);
        }
    }

    public abstract T execute() throws Exception;

    public final T start() throws SQLException {
        try {
            Future<T> future = POOL.submit(this);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException || e instanceof TimeoutException) {
                handleTimeout();
                throw new SQLTimeoutException("Operation timed out (timeout = " + timeoutSeconds + "s)", e);
            }

            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof SQLException) {
                    throw (SQLException) cause;
                } else {
                    throw new SQLException("Error processing request: " + cause.getMessage(), cause);
                }
            }
            throw new SQLException("Error processing request: " + e.getMessage(), e);
        }
    }

    protected abstract void handleTimeout();
}
