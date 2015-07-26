package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigType;

public class DBMethodJdwpRunConfigType extends DBMethodRunConfigType<DBMethodJdwpRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Method Runner (JDWP)";
    private DBMethodJdwpRunConfigFactory[] configurationFactories = new DBMethodJdwpRunConfigFactory[]{new DBMethodJdwpRunConfigFactory(this)};


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

    public DBMethodJdwpRunConfigFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }
}
