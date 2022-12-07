package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.common.thread.Threads;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.connection.Resources;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.*;

import static com.dci.intellij.dbn.common.exception.Exceptions.causeOf;

public final class StatementExecutor {
    private StatementExecutor() {}

    public static <T> T execute(StatementExecutorContext context, Callable<T> callable) throws SQLException {
        long start = System.currentTimeMillis();
        int timeout = context.getTimeout();
        try {
            ExecutorService executorService = Threads.databaseInterfaceExecutor();
            Future<T> future = executorService.submit(callable);
            T result = Timeout.waitFor(future, timeout, TimeUnit.SECONDS);

            context.log("QUERY", false, false, millisSince(start));
            return result;

        } catch (TimeoutException | InterruptedException | RejectedExecutionException e) {
            context.log("QUERY", false, true, millisSince(start));
            Resources.close(context.getStatement());
            throw new SQLTimeoutException("Operation timed out (timeout = " + timeout + "s)", e);

        } catch (ExecutionException e) {
            context.log("QUERY", true, false, millisSince(start));
            Resources.close(context.getStatement());
            Throwable cause = causeOf(e);
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else {
                throw new SQLException("Error processing request: " + cause.getMessage(), cause);
            }
        } catch (Throwable e) {
            context.log("QUERY", true, false, millisSince(start));
            Resources.close(context.getStatement());
            throw new SQLException("Error processing request: " + e.getMessage(), e);

        }
    }

    private static long millisSince(long start) {
        return System.currentTimeMillis() - start;
    }
}
