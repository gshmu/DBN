package com.dci.intellij.dbn.database.common.execution;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface MethodExecutionProcessor {
    void execute(MethodExecutionInput executionInput, DBDebuggerType debuggerType) throws SQLException;

    void execute(MethodExecutionInput executionInput, DBNConnection connection, DBDebuggerType debuggerType) throws SQLException;

    @NotNull
    DBMethod getMethod();
}
