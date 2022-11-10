package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseExecutionInterface {
    MethodExecutionProcessor createExecutionProcessor(DBMethod method);
    MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method);

    CmdLineExecutionInput createScriptExecutionInput(
            @NotNull CmdLineInterface cmdLineInterface,
            @NotNull String filePath,
            String content,
            @Nullable SchemaId schemaId,
            @NotNull DatabaseInfo databaseInfo,
            @NotNull AuthenticationInfo authenticationInfo);
}
