package com.dci.intellij.dbn.database.mysql;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public class MySqlExecutionInterface extends DatabaseExecutionInterfaceImpl {

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return createSimpleMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Nullable
    @Override
    public String getDefaultCmdLineInterface() {
        //return "C:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\mysql.exe";
        return "mysql";
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(@Nullable String programPath, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        ScriptExecutionInput executionInput = new ScriptExecutionInput(content);

        List<String> command = executionInput.getCommand();
        command.add(CommonUtil.nvl(programPath, getDefaultCmdLineInterface()));
        command.add("--force");
        command.add("--verbose");
        command.add("--host=" + databaseInfo.getHost());

        String port = databaseInfo.getPort();
        if (StringUtil.isNotEmpty(port)) command.add("--port=" + port);

        command.add("--user=" + authenticationInfo.getUser());
        command.add("--password=" + authenticationInfo.getPassword());

        command.add("-e");
        command.add("\"source " + filePath + "\"");

        String database = databaseInfo.getDatabase();
        if (StringUtil.isNotEmpty(database)) command.add(database);

        //command.add("< " + filePath);

        StringBuilder contentBuilder = executionInput.getContent();
        if (StringUtil.isNotEmpty(schema)) {
            contentBuilder.insert(0, "use " + schema + ";\n");
        }
        //contentBuilder.append("\nexit;\n");
        return executionInput;
    }
}