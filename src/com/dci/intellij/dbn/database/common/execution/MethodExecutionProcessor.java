package com.dci.intellij.dbn.database.common.execution;

import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public interface MethodExecutionProcessor<T extends DBMethod> {
    void execute(MethodExecutionInput executionInput, boolean debug) throws SQLException;

    void execute(MethodExecutionInput executionInput, Connection connection, boolean debug) throws SQLException;

    @NotNull
    T getMethod();
}
