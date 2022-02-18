package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class Savepoints<T>{
    private Savepoints() {}

    public static <R> R call(@NotNull DBNResultSet resultSet, ThrowableCallable<R, SQLException> callback) throws SQLException {
        DBNConnection connection = resultSet.getConnection();
        return call(connection, callback);
    }

    public static <R> R call(DBNConnection connection, ThrowableCallable<R, SQLException> callable) throws SQLException {
        if (connection == null) {
            return callable.call();
        } else {
            return connection.withSavepoint(callable);
        }
    }

    public static <R> void run(@NotNull DBNResultSet resultSet, ThrowableRunnable<SQLException> runnable) throws SQLException {
        DBNConnection connection = resultSet.getConnection();
        run(connection, runnable);
    }

    public static <R> void run(@Nullable DBNConnection connection, ThrowableRunnable<SQLException> runnable) throws SQLException {
        if (connection == null) {
            runnable.run();
        } else {
            connection.withSavepoint(runnable);
        }
    }
}
