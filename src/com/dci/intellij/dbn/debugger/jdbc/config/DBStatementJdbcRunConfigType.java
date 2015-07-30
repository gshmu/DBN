package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;

public class DBStatementJdbcRunConfigType extends DBStatementRunConfigType<DBStatementJdbcRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner";
    private DBStatementJdbcRunConfigFactory[] configurationFactories = new DBStatementJdbcRunConfigFactory[]{new DBStatementJdbcRunConfigFactory(this)};


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

    public DBStatementJdbcRunConfigFactory[] getConfigurationFactories() {
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
