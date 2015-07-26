package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigType;

public class DBMethodJdbcRunConfigType extends DBMethodRunConfigType<DBMethodJdbcRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner";
    private DBMethodJdbcRunConfigFactory[] configurationFactories = new DBMethodJdbcRunConfigFactory[]{new DBMethodJdbcRunConfigFactory(this)};


    @Override
    public String getDisplayName() {
        return "DB Method";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "DB Navigator - Method Runner";
    }

    @Override
    public Icon getIcon() {
        return Icons.EXEC_METHOD_CONFIG;
    }

    @Override
    @NotNull
    public String getId() {
        return "DBNMethodRunConfiguration";
    }

    @Override
    public DBMethodJdbcRunConfigFactory[] getConfigurationFactories() {
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
