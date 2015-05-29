package com.dci.intellij.dbn.debugger.execution.method;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;

public class DBMethodRunConfigurationType extends DBProgramRunConfigurationType {
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBMethodRunConfigurationFactory(this)};


    public String getDisplayName() {
        return "Database Method (DBN)";
    }

    public String getConfigurationTypeDescription() {
        return null;
    }

    public Icon getIcon() {
        return Icons.EXEC_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBNMethodRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    public DBMethodRunConfigurationFactory getConfigurationFactory() {
        return (DBMethodRunConfigurationFactory) configurationFactories[0];
    }
}
