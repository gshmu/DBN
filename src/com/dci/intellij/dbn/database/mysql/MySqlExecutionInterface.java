package com.dci.intellij.dbn.database.mysql;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.database.CmdLineExecutionInput;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MySqlExecutionInterface extends DatabaseExecutionInterfaceImpl {

    @Override
    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return createSimpleMethodExecutionProcessor(method);
    }

    @Override
    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Override
    public CmdLineExecutionInput createScriptExecutionInput(
            @NotNull CmdLineInterface cmdLineInterface,
            @NotNull String filePath,
            String content,
            @Nullable SchemaId schemaId,
            @NotNull DatabaseInfo databaseInfo,
            @NotNull AuthenticationInfo authenticationInfo) {

        CmdLineExecutionInput executionInput = new CmdLineExecutionInput(content);

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
        if (schemaId != null) {
            contentBuilder.insert(0, "use " + schemaId + ";\n");
        }
        //contentBuilder.append("\nexit;\n");
        return executionInput;
    }
}