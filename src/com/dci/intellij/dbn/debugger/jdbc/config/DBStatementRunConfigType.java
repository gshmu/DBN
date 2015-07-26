package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfigType;

public class DBStatementRunConfigType extends DBProgramRunConfigType<DBStatementRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner";
    private DBStatementRunConfigFactory[] configurationFactories = new DBStatementRunConfigFactory[]{new DBStatementRunConfigFactory(this)};


    public String getDisplayName() {
        return "DB Statement";
    }

    public String getConfigurationTypeDescription() {
        return "DB Navigator - Statement Runner";
    }

    public Icon getIcon() {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBNStatementRunConfiguration";
    }

    public DBStatementRunConfigFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }

    @Override
    public DBDebuggerType getDebuggerType() {
        return DBDebuggerType.JDBC;
    }
}
