package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public interface DatabaseExecutionInterface {
    MethodExecutionProcessor createExecutionProcessor(DBMethod method);
    MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method);
    ScriptExecutionInput createScriptExecutionInput(String programPath, String filePath, String content, DatabaseInfo databaseInfo, AuthenticationInfo authenticationInfo);
}
