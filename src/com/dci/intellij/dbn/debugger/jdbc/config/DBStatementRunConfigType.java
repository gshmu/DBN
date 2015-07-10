package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.execution.configurations.ConfigurationFactory;

public class DBStatementRunConfigType extends DBProgramRunConfigurationType {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner";
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBStatementRunConfigFactory(this)};


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

    public DBStatementRunConfigFactory getConfigurationFactory() {
        return (DBStatementRunConfigFactory) configurationFactories[0];
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
