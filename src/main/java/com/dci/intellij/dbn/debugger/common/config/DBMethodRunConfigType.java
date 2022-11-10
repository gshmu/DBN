package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.jdbc.config.DBMethodJdbcRunConfigFactory;
import com.dci.intellij.dbn.debugger.jdwp.config.DBMethodJdwpRunConfigFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBMethodRunConfigType extends DBRunConfigType<DBMethodRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner";
    private DBMethodRunConfigFactory[] configurationFactories = new DBMethodRunConfigFactory[]{
            new DBMethodJdbcRunConfigFactory(this),
            new DBMethodJdwpRunConfigFactory(this)};


    @NotNull
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
    public DBMethodRunConfigFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }

    @Override
    public DBMethodRunConfigFactory getConfigurationFactory(DBDebuggerType debuggerType) {
        for (DBMethodRunConfigFactory configurationFactory : configurationFactories) {
            if (configurationFactory.getDebuggerType() == debuggerType) {
                return configurationFactory;
            }
        }
        return null;
    }
}
