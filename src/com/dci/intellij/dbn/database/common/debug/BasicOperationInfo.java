package com.dci.intellij.dbn.database.common.debug;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.dci.intellij.dbn.database.common.statement.CallableStatementOutput;

public class BasicOperationInfo implements CallableStatementOutput {
    protected String error;

    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.VARCHAR);
    }

    public void read(CallableStatement statement) throws SQLException {
        error = statement.getString(1);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
