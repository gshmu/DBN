package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.execution.configurations.ConfigurationFactory;

public class DBMethodRunConfigType extends DBProgramRunConfigurationType {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner";
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBMethodRunConfigFactory(this)};


    public String getDisplayName() {
        return "DB Method";
    }

    public String getConfigurationTypeDescription() {
        return "DB Navigator - Method Runner";
    }

    public Icon getIcon() {
        return Icons.EXEC_METHOD_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBNMethodRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public DBMethodRunConfigFactory getConfigurationFactory() {
        return (DBMethodRunConfigFactory) configurationFactories[0];
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
