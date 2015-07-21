package com.dci.intellij.dbn.database.common.execution;

import com.dci.intellij.dbn.execution.ExecutionType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface MethodExecutionProcessor<T extends DBMethod> {
    void execute(MethodExecutionInput executionInput, ExecutionType executionType) throws SQLException;

    void execute(MethodExecutionInput executionInput, Connection connection, ExecutionType executionType) throws SQLException;

    @NotNull
    T getMethod();
}
