package com.dci.intellij.dbn.connection.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionUtil;

public abstract class TransactionSavepointCall<T>{
    private Connection connection;


    public TransactionSavepointCall(@Nullable Connection connection) {
        this.connection = connection;
    }

    public T start() throws Exception {
        if (connection != null) {
            Savepoint savepoint = ConnectionUtil.createSavepoint(connection);
            try {
                return execute();
            } catch (SQLException e) {
                ConnectionUtil.rollback(connection, savepoint);
                throw e;
            } finally {
                ConnectionUtil.releaseSavepoint(connection, savepoint);
            }
         } else {
            return execute();
        }
    }

    public abstract T execute() throws Exception;
}
