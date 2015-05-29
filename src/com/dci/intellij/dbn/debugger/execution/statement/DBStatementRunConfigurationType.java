package com.dci.intellij.dbn.debugger.execution.statement;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;

public class DBStatementRunConfigurationType extends DBProgramRunConfigurationType {
    private ConfigurationFactory[] configurationFactories = new ConfigurationFactory[]{new DBStatementRunConfigurationFactory(this)};


    public String getDisplayName() {
        return "DB-Method";
    }

    public String getConfigurationTypeDescription() {
        return null;
    }

    public Icon getIcon() {
        return Icons.EXEC_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBMethodRunSession";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    public DBStatementRunConfigurationFactory getConfigurationFactory() {
        return (DBStatementRunConfigurationFactory) configurationFactories[0];
    }
}
