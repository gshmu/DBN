package com.dci.intellij.dbn.connection.jdbc;

import java.sql.PreparedStatement;

public class DBNPreparedStatement<T extends PreparedStatement> extends DBNPreparedStatementBase<T> {
    public DBNPreparedStatement(T inner, DBNConnection connection) {
        super(inner, connection);
    }
}
