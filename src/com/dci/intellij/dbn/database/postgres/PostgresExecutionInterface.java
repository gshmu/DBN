package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.postgres.execution.PostgresMethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public class PostgresExecutionInterface extends DatabaseExecutionInterfaceImpl {

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new PostgresMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(String programPath, String filePath, String content, DatabaseInfo databaseInfo, AuthenticationInfo authenticationInfo) {
        return null;
    }
}