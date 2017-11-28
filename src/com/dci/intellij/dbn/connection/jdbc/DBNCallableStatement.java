package com.dci.intellij.dbn.connection.jdbc;

import java.sql.CallableStatement;

public class DBNCallableStatement extends DBNCallableStatementBase{
    public DBNCallableStatement(CallableStatement inner, DBNConnection connection) {
        super(inner, connection);
    }
}
