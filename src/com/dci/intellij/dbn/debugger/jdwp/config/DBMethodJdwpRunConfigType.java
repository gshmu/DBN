package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.jdbc.config.DBProgramRunConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;

public class DBMethodJdwpRunConfigType extends DBProgramRunConfigurationType {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner (JDWP)";
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBMethodJdwpRunConfigFactory(this)};


    public String getDisplayName() {
        return "DB Method (JDWP)";
    }

    public String getConfigurationTypeDescription() {
        return "DB Navigator - Method Runner (JDWP)";
    }

    public Icon getIcon() {
        return Icons.EXEC_METHOD_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBNMethodJdwpRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public DBMethodJdwpRunConfigFactory getConfigurationFactory() {
        return (DBMethodJdwpRunConfigFactory) configurationFactories[0];
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
