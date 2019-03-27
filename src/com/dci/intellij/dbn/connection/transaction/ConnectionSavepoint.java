package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Savepoint;

public interface ConnectionSavepoint<T>{
    static <R> R call(DBNResultSet resultSet, ThrowableCallable<R, SQLException> callback) throws SQLException {
        DBNConnection connection = resultSet.getStatement().getConnection();
        return call(connection, callback);
    }

    static <R> R call(DBNConnection connection, ThrowableCallable<R, SQLException> callable) throws SQLException {
        if (connection == null) {
            return callable.call();
        } else {
            synchronized (connection) {
                Savepoint savepoint = ResourceUtil.createSavepoint(connection);
                try {
                    return callable.call();
                } catch (SQLException e) {
                    ResourceUtil.rollbackSilently(connection, savepoint);
                    throw e;
                } finally {
                    ResourceUtil.releaseSavepoint(connection, savepoint);
                }
            }
        }
    }

    static <R> void run(DBNResultSet resultSet, ThrowableRunnable<SQLException> runnable) throws SQLException {
        DBNConnection connection = resultSet.getStatement().getConnection();
        run(connection, runnable);
    }

    static <R> void run(@Nullable DBNConnection connection, ThrowableRunnable<SQLException> runnable) throws SQLException {
        if (connection == null) {
            runnable.run();
        } else {
            synchronized (connection) {
                Savepoint savepoint = ResourceUtil.createSavepoint(connection);
                try {
                    runnable.run();
                } catch (SQLException e) {
                    ResourceUtil.rollbackSilently(connection, savepoint);
                    throw e;
                } finally {
                    ResourceUtil.releaseSavepoint(connection, savepoint);

                }
            }
        }
    }
}
