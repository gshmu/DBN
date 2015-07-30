package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;

public class DBStatementJdwpRunConfigType extends DBStatementRunConfigType<DBStatementJdwpRunConfigFactory> {
    public static final String DEFAULT_RUNNER_NAME = "DB Statement Runner (JDWP)";
    private DBStatementJdwpRunConfigFactory[] configurationFactories = new DBStatementJdwpRunConfigFactory[]{new DBStatementJdwpRunConfigFactory(this)};


    public String getDisplayName() {
        return "DB Statement (JDWP)";
    }

    public String getConfigurationTypeDescription() {
        return "DB Navigator - Statement Runner (JDWP)";
    }

    public Icon getIcon() {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @NotNull
    public String getId() {
        return "DBNStatementJdwpRunConfiguration";
    }

    public DBStatementJdwpRunConfigFactory[] getConfigurationFactories() {
        return configurationFactories;
    }

    @Override
    public String getDefaultRunnerName() {
        return DEFAULT_RUNNER_NAME;
    }

    @Override
    public DBDebuggerType getDebuggerType() {
        return DBDebuggerType.JDWP;
    }
}
