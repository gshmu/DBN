package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.routine.ManagedCallable;
import com.dci.intellij.dbn.common.routine.ManagedRunnable;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;

import java.sql.SQLException;
import java.sql.Savepoint;

public abstract class ConnectionSavepointCall<T>{
    private final DBNConnection connection;


    private ConnectionSavepointCall(DBNResultSet resultSet) throws SQLException {
        this(resultSet.getStatement().getConnection());
    }

    private ConnectionSavepointCall(DBNConnection connection) {
        this.connection = connection;
    }

    public T start() throws SQLException {
        if (connection == null) {
            return execute();
        } else {
            synchronized (connection) {
                Savepoint savepoint = ConnectionUtil.createSavepoint(connection);
                try {
                    return execute();
                } catch (SQLException e) {
                    ConnectionUtil.rollbackSilently(connection, savepoint);
                    throw e;
                } finally {
                    ConnectionUtil.releaseSavepoint(connection, savepoint);
                }
            }
        }
    }

    public abstract T execute() throws SQLException;

    public static <R> R invoke(DBNResultSet resultSet, ManagedCallable<R, SQLException> callable) throws SQLException {
        return new ConnectionSavepointCall<R>(resultSet) {
            @Override
            public R execute() throws SQLException {
                return callable.call();
            }
        }.start();
    }

    public static <R> R invoke(DBNConnection connection, ManagedCallable<R, SQLException> callable) throws SQLException {
        return new ConnectionSavepointCall<R>(connection) {
            @Override
            public R execute() throws SQLException {
                return callable.call();
            }
        }.start();
    }

    public static <R> void invoke(DBNResultSet resultSet, ManagedRunnable<SQLException> runnable) throws SQLException {
        new ConnectionSavepointCall<R>(resultSet) {
            @Override
            public R execute() throws SQLException {
                runnable.run();
                return null;
            }
        }.start();
    }

    public static <R> void invoke(DBNConnection connection, ManagedRunnable<SQLException> runnable) throws SQLException {
        new ConnectionSavepointCall<R>(connection) {
            @Override
            public R execute() throws SQLException {
                runnable.run();
                return null;
            }
        }.start();
    }

}
