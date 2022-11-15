package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.connection.util.Jdbc.Callable;
import com.dci.intellij.dbn.connection.util.Jdbc.Runnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class Savepoints{
    private Savepoints() {}

    public static <T> T call(@NotNull DBNResultSet resultSet, Callable<T> callable) throws SQLException {
        DBNConnection c = resultSet.getConnection();
        return call(c, callable);
    }

    public static <T> T call(DBNConnection conn, Callable<T> callable) throws SQLException {
        if (conn == null) {
            return callable.call();
        } else {
            return conn.withSavepoint(callable);
        }
    }

    public static void run(@NotNull DBNResultSet resultSet, Runnable runnable) throws SQLException {
        DBNConnection conn = resultSet.getConnection();
        run(conn, runnable);
    }

    public static void run(@Nullable DBNConnection conn, Runnable runnable) throws SQLException {
        if (conn == null) {
            runnable.run();
        } else {
            conn.withSavepoint(runnable);
        }
    }
}
