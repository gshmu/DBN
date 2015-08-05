package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigCategory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBStatementJdwpRunConfigFactory extends DBStatementRunConfigFactory<DBStatementRunConfigType, DBStatementRunConfig> {
    public DBStatementJdwpRunConfigFactory(@NotNull DBStatementRunConfigType type) {
        super(type, DBDebuggerType.JDWP);
    }

    @Override
    public DBStatementJdwpRunConfig createConfiguration(Project project, String name, DBRunConfigCategory category) {
        return new DBStatementJdwpRunConfig(project, this, name, category);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        return Icons.EXEC_STATEMENT_CONFIG;
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBStatementJdwpRunConfig(project, this, "", DBRunConfigCategory.TEMPLATE);
    }

    @Override
    public String getName() {
        return super.getName() + "(JDWP)";
    }

}
