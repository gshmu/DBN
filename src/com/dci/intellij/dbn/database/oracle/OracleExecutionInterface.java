package com.dci.intellij.dbn.database.oracle;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.oracle.execution.OracleMethodDebugExecutionProcessor;
import com.dci.intellij.dbn.database.oracle.execution.OracleMethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public class OracleExecutionInterface implements DatabaseExecutionInterface {
    private static final String SQLPLUS_CONNECT_PATTERN = "\"[USER]/[PASSWORD]@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=[HOST])(Port=[PORT]))(CONNECT_DATA=(SID=[DATABASE])))\"";

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new OracleMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return new OracleMethodDebugExecutionProcessor(method);
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(@Nullable String programPath, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        ScriptExecutionInput executionInput = new ScriptExecutionInput(content);
        String connectArg = SQLPLUS_CONNECT_PATTERN.
                replace("[USER]", authenticationInfo.getUser()).
                replace("[PASSWORD]", authenticationInfo.getPassword()).
                replace("[HOST]", databaseInfo.getHost()).
                replace("[PORT]", databaseInfo.getPort()).
                replace("[DATABASE]", databaseInfo.getDatabase());

        String fileArg = "\"@" + filePath + "\"";

        List<String> command = executionInput.getCommand();
        command.add(CommonUtil.nvl(programPath, "sqlplus"));
        command.add(connectArg);
        command.add(fileArg);

        StringBuilder contentBuilder = executionInput.getContent();
        if (StringUtil.isNotEmpty(schema)) {
            contentBuilder.insert(0, "alter session set current_schema = " + schema + ";\n");
        }
        contentBuilder.insert(0, "set echo on;\n");
        contentBuilder.append("\nexit;\n");

        return executionInput;
    }
}
