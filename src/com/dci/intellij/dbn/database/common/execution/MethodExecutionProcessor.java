package com.dci.intellij.dbn.database.common.execution;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;

public interface MethodExecutionProcessor<T extends DBMethod> {
    void execute(MethodExecutionInput executionInput, DBDebuggerType debuggerType) throws SQLException;

    void execute(MethodExecutionInput executionInput, DBNConnection connection, DBDebuggerType debuggerType) throws SQLException;

    @NotNull
    T getMethod();
}
