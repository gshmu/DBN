package com.dci.intellij.dbn.database.common.statement;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class StatementExecutionTimeoutCall<T> implements Callable<T>{
    private long timeoutSeconds;

    public StatementExecutionTimeoutCall(long timeoutSeconds) {
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
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<T> future = executor.submit(this);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof InterruptedException || e instanceof TimeoutException) {
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
}
