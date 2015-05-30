package com.dci.intellij.dbn.debugger.execution.statement;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class DBStatementRunConfigurationType extends DBProgramRunConfigurationType {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner";
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBStatementRunConfigurationFactory(this)};


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

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    public DBStatementRunConfigurationFactory getConfigurationFactory() {
        return (DBStatementRunConfigurationFactory) configurationFactories[0];
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
