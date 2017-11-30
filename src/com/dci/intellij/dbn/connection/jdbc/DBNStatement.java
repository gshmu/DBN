package com.dci.intellij.dbn.connection.jdbc;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBNStatement<T extends Statement> extends DBNStatementBase<T>{
    private WeakReference<DBNConnection> connection;
    private WeakReference<DBNResultSet> resultSet;

    public DBNStatement(T inner, DBNConnection connection) {
        super(inner);
        this.connection = new WeakReference<>(connection);
    }

    @Override
    public DBNConnection getConnection() {
        return connection.get();
    }


    @Override
    public boolean isCancelledInner() throws SQLException {
        return false;
    }

    @Override
    public void cancelInner() throws SQLException {
        inner.cancel();
    }

    @Override
    public boolean isClosedInner() throws SQLException {
        return inner.isClosed();
    }

    @Override
    public void closeInner() throws SQLException {
        inner.close();
    }

    @Override
    public void close() {
        try {
            super.close();
        } finally {
            DBNConnection connection = this.connection.get();
            if (connection != null) {
                connection.release(this);
            }
        }
    }

    @Override
    protected DBNResultSet wrap(ResultSet original) {
        if (original == null) {
            resultSet = null;
        } else {
            if (resultSet == null) {
                resultSet = new WeakReference<>(new DBNResultSet(original, this));
            } else {
                DBNResultSet wrapped = resultSet.get();
                if (wrapped == null || wrapped.inner != original) {
                    resultSet = new WeakReference<>(new DBNResultSet(original, this));
                }
            }
        }
        return this.resultSet == null ? null : this.resultSet.get();
    }
}
