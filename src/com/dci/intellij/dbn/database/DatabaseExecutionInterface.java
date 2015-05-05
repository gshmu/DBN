package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public interface DatabaseExecutionInterface {
    MethodExecutionProcessor createExecutionProcessor(DBMethod method);
    MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method);
    ScriptExecutionInput createScriptExecutionInput(@Nullable String programPath, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo);
}
