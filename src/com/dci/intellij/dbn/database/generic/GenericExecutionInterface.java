package com.dci.intellij.dbn.database.generic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.object.DBMethod;

public class GenericExecutionInterface extends DatabaseExecutionInterfaceImpl {

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return createSimpleMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Nullable
    @Override
    public CmdLineInterface getDefaultCmdLineInterface() {
        return null;
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(@NotNull String programPath, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        throw new UnsupportedOperationException();
    }
}
