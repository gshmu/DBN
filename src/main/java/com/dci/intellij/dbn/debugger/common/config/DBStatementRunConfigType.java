package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.jdbc.config.DBStatementJdbcRunConfigFactory;
import com.dci.intellij.dbn.debugger.jdwp.config.DBStatementJdwpRunConfigFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBStatementRunConfigType extends DBRunConfigType<DBStatementRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner";
    private DBStatementRunConfigFactory[] configurationFactories = new DBStatementRunConfigFactory[]{
            new DBStatementJdbcRunConfigFactory(this),
            new DBStatementJdwpRunConfigFactory(this)};


    @NotNull
    @Override
    public String getDisplayName() {
        return "DB Statement";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "DB Navigator - Statement Runner";
    }

    @Override
    public Icon getIcon() {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    @NotNull
    public String getId() {
        return "DBNStatementRunConfiguration";
    }

    @Override
    public DBStatementRunConfigFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }

    @Override
    public DBStatementRunConfigFactory getConfigurationFactory(DBDebuggerType debuggerType) {
        for (DBStatementRunConfigFactory configurationFactory : configurationFactories) {
            if (configurationFactory.getDebuggerType() == debuggerType) {
                return configurationFactory;
            }
        }
        return null;
    }
}
