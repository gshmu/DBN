package com.dci.intellij.dbn.database.common.debug;

import com.dci.intellij.dbn.database.common.statement.CallableStatementOutput;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class DebuggerVersionInfo implements CallableStatementOutput {
    private String version;


    public String getVersion() {
        return version;
    }

    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.VARCHAR);
    }

    public void read(CallableStatement statement) throws SQLException {
        version = statement.getString(1);
    }
}
