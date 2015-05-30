package com.dci.intellij.dbn.debugger.execution.method;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class DBMethodRunConfigurationType extends DBProgramRunConfigurationType {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner";
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBMethodRunConfigurationFactory(this)};


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
    public DBMethodRunConfigurationFactory getConfigurationFactory() {
        return (DBMethodRunConfigurationFactory) configurationFactories[0];
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
