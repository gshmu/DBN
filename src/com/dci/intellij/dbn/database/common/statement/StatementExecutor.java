package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.common.thread.ThreadPool;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import lombok.Data;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.concurrent.*;

public final class StatementExecutor {
    private StatementExecutor() {}

    public static <T> T execute(Context context, Callable<T> callable) throws SQLException {
        long start = System.currentTimeMillis();
        String identifier = context.getIdentifier();
        int timeout = context.getTimeout();
        DiagnosticBundle diagnostics = context.getDiagnostics();
        try {
            ExecutorService executorService = ThreadPool.databaseInterfaceExecutor();
            Future<T> future = executorService.submit(callable);
            T result = future.get(timeout, TimeUnit.SECONDS);

            diagnostics.log(identifier, false, false, millisSince(start));
            return result;

        } catch (InterruptedException | TimeoutException e) {
            diagnostics.log(identifier, false, true, millisSince(start));
            ResourceUtil.close(context.getStatement());
            throw new SQLTimeoutException("Operation timed out (timeout = " + timeout + "s)", e);

        } catch (ExecutionException e) {
            diagnostics.log(identifier, true, false, millisSince(start));
            ResourceUtil.close(context.getStatement());
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException) cause;
            } else {
                throw new SQLException("Error processing request: " + cause.getMessage(), cause);
            }
        } catch (Throwable e) {
            diagnostics.log(identifier, true, false, millisSince(start));
            ResourceUtil.close(context.getStatement());
            throw new SQLException("Error processing request: " + e.getMessage(), e);

        }
    }

    private static long millisSince(long start) {
        return System.currentTimeMillis() - start;
    }

    public static Context context(DiagnosticBundle diagnostics, String identifier, int timeout) {
        return new Context(diagnostics, identifier, timeout);
    }

    @Data
    public static final class Context {
        private Statement statement;
        private final DiagnosticBundle diagnostics;
        private final String identifier;
        private final int timeout;

        public Context(DiagnosticBundle diagnostics, String identifier, int timeout) {
            this.diagnostics = diagnostics;
            this.identifier = identifier;
            this.timeout = timeout;
        }
    }
}
