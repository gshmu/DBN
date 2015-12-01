package com.dci.intellij.dbn.connection.transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import com.dci.intellij.dbn.connection.ConnectionUtil;

public abstract class ConnectionSavepointCall<T>{
    private final Connection connection;
    private ThreadLocal<ConnectionSavepointCall> threadSavepointCall = new ThreadLocal<ConnectionSavepointCall>();


    public ConnectionSavepointCall(ResultSet resultSet) throws SQLException {
        this(resultSet.getStatement().getConnection());
    }

    public ConnectionSavepointCall(Connection connection) {
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
}
