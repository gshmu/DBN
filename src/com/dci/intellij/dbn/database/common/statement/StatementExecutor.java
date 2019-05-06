package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.dci.intellij.dbn.connection.ResourceUtil;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public interface StatementExecutor {
    static <T> T execute(int timeout, AtomicReference<Statement> statement, Callable<T> callable) throws SQLException {
        try {
            ExecutorService executorService = ThreadFactory.databaseInterfaceExecutor();
            Future<T> future = executorService.submit(callable);
            return future.get(timeout, TimeUnit.SECONDS);

        } catch (InterruptedException | TimeoutException e) {
            ResourceUtil.close(statement.get());
            throw new SQLTimeoutException("Operation timed out (timeout = " + timeout + "s)", e);

        } catch (ExecutionException e) {
            ResourceUtil.close(statement.get());
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else {
                throw new SQLException("Error processing request: " + cause.getMessage(), cause);
            }
        } catch (Throwable e) {
            ResourceUtil.close(statement.get());
            throw new SQLException("Error processing request: " + e.getMessage(), e);

        }
    }
}
