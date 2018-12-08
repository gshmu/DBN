package com.dci.intellij.dbn.connection.transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import com.dci.intellij.dbn.common.util.CustomCallable;
import com.dci.intellij.dbn.common.util.CustomRunnable;
import com.dci.intellij.dbn.connection.ConnectionUtil;

public abstract class ConnectionSavepointCall<T>{
    private final Connection connection;
    private ThreadLocal<ConnectionSavepointCall> threadSavepointCall = new ThreadLocal<ConnectionSavepointCall>();


    private ConnectionSavepointCall(ResultSet resultSet) throws SQLException {
        this(resultSet.getStatement().getConnection());
    }

    private ConnectionSavepointCall(Connection connection) {
        this.connection = connection;
    }

    public T start() throws SQLException {
        if (connection == null) {
            return execute();
        } else {
            synchronized (connection) {
                Savepoint savepoint = ConnectionUtil.createSavepoint(connection);
                try {
                    threadSavepointCall.set(this);
                    return execute();
                } catch (SQLException e) {
                    ConnectionUtil.rollback(connection, savepoint);
                    throw e;
                } finally {
                    threadSavepointCall.set(null);
                    ConnectionUtil.releaseSavepoint(connection, savepoint);
                }
            }
        }
    }

    public abstract T execute() throws SQLException;

    public static <R> R invoke(ResultSet resultSet, CustomCallable<R, SQLException> callable) throws SQLException {
        return new ConnectionSavepointCall<R>(resultSet) {
            @Override
            public R execute() throws SQLException {
                return callable.call();
            }
        }.start();
    }

    public static <R> R invoke(Connection connection, CustomCallable<R, SQLException> callable) throws SQLException {
        return new ConnectionSavepointCall<R>(connection) {
            @Override
            public R execute() throws SQLException {
                return callable.call();
            }
        }.start();
    }

    public static <R> void invoke(ResultSet resultSet, CustomRunnable<SQLException> runnable) throws SQLException {
        new ConnectionSavepointCall<R>(resultSet) {
            @Override
            public R execute() throws SQLException {
                runnable.run();
                return null;
            }
        }.start();
    }

    public static <R> void invoke(Connection connection, CustomRunnable<SQLException> runnable) throws SQLException {
        new ConnectionSavepointCall<R>(connection) {
            @Override
            public R execute() throws SQLException {
                runnable.run();
                return null;
            }
        }.start();
    }

}
