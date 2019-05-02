package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.dci.intellij.dbn.common.util.Traceable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class StatementExecutor<T> extends Traceable implements Callable<T>{

    private long timeoutSeconds;

    public StatementExecutor(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public final T call() throws Exception {
        trace(this);
        return execute();
    }

    public abstract T execute() throws Exception;

    public final T start() throws SQLException {
        try {
            ExecutorService executorService = ThreadFactory.databaseInterfaceExecutor();
            Future<T> future = executorService.submit(this);
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
