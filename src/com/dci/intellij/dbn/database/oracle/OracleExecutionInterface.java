package com.dci.intellij.dbn.database.oracle;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.oracle.execution.OracleMethodDebugExecutionProcessor;
import com.dci.intellij.dbn.database.oracle.execution.OracleMethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public class OracleExecutionInterface implements DatabaseExecutionInterface {
    private static final String SQLPLUS_COMMAND_PATTERN = "[PROGRAM_PATH] \"[USER]/[PASSWORD]@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=[HOST])(Port=[PORT]))(CONNECT_DATA=(SID=[DATABASE])))\" @[SCRIPT_FILE_PATH]";

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new OracleMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return new OracleMethodDebugExecutionProcessor(method);
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(String programPath, DatabaseInfo databaseInfo, AuthenticationInfo authenticationInfo, String filePath) {
        String command = SQLPLUS_COMMAND_PATTERN.
                replace("[PROGRAM_PATH]", programPath).
                replace("[USER]", authenticationInfo.getUser()).
                replace("[PASSWORD]", authenticationInfo.getPassword()).
                replace("[HOST]", databaseInfo.getHost()).
                replace("[PORT]", databaseInfo.getPort()).
                replace("[DATABASE]", databaseInfo.getDatabase()).
                replace("[SCRIPT_FILE_PATH]", filePath);

        return new ScriptExecutionInput(command, null);
    }
}
