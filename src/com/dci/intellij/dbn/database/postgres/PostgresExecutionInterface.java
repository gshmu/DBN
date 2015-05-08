package com.dci.intellij.dbn.database.postgres;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.postgres.execution.PostgresMethodExecutionProcessor;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.object.DBMethod;

public class PostgresExecutionInterface extends DatabaseExecutionInterfaceImpl {
    public static final CmdLineInterface DEFAULT_CMD_LINE_INTERFACE = new CmdLineInterface(DatabaseType.MYSQL, "psql ", "PostgreSQL interactive terminal - psql", "environment path based");

    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return new PostgresMethodExecutionProcessor(method);
    }

    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Nullable
    @Override
    public CmdLineInterface getDefaultCmdLineInterface() {
        return DEFAULT_CMD_LINE_INTERFACE;
    }

    @Override
    public ScriptExecutionInput createScriptExecutionInput(@NotNull CmdLineInterface cmdLineInterface, @NotNull String filePath, String content, @Nullable String schema, @NotNull DatabaseInfo databaseInfo, @NotNull AuthenticationInfo authenticationInfo) {
        return null;
    }
}