package com.dci.intellij.dbn.connection.jdbc;

import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBNResultSet extends DBNResultSetBase {
    private WeakReference<DBNStatement> statement;

    public DBNResultSet(ResultSet inner, DBNStatement statement) {
        super(inner);
        this.statement = new WeakReference<>(statement);
    }

    @Override
    public Statement getStatement() throws SQLException {
        return statement.get();
    }
}
