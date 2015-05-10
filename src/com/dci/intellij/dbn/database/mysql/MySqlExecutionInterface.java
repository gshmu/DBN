package com.dci.intellij.dbn.database.mysql;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.ScriptExecutionCommand;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MySqlExecutionInterface extends DatabaseExecutionInterfaceImpl {

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return createSimpleMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Override
    public ScriptExecutionCommand createScriptExecutionInput(@NotNull CmdLineInterface cmdLineInterface, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        ScriptExecutionCommand executionInput = new ScriptExecutionCommand(content);

        List<String> command = executionInput.getCommand();
        command.add(cmdLineInterface.getExecutablePath());
        command.add("--force");
        command.add("--verbose");
        command.add("--host=" + databaseInfo.getHost());

        String port = databaseInfo.getPort();
        if (StringUtil.isNotEmpty(port)) {
            command.add("--port=" + port);
        }

        String database = databaseInfo.getDatabase();
        if (StringUtil.isNotEmpty(database)) {
            command.add("--database=" + database);
        }


        command.add("--user=" + authenticationInfo.getUser());
        command.add("--password=" + authenticationInfo.getPassword());

        command.add("-e");
        command.add("\"source " + filePath + "\"");

        //command.add("< " + filePath);

        StringBuilder contentBuilder = executionInput.getContent();
        if (StringUtil.isNotEmpty(schema)) {
            contentBuilder.insert(0, "use " + schema + ";\n");
        }
        //contentBuilder.append("\nexit;\n");
        return executionInput;
    }
}