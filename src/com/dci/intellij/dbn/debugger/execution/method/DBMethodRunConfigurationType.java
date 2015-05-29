package com.dci.intellij.dbn.debugger.execution.method;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;

public class DBMethodRunConfigurationType implements ConfigurationType {
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBMethodRunConfigurationFactory(this)};


    public String getDisplayName() {
        return "DB-Program";
    }

    public String getConfigurationTypeDescription() {
        return null;
    }

    public Icon getIcon() {
        return Icons.EXEC_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBProgramDebugSession";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    public DBMethodRunConfigurationFactory getConfigurationFactory() {
        return (DBMethodRunConfigurationFactory) configurationFactories[0];
    }
}
