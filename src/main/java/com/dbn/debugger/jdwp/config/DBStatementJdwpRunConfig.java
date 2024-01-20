package com.dbn.debugger.jdwp.config;

import com.dbn.debugger.DBDebuggerType;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.dbn.debugger.common.config.DBRunConfigCategory;
import com.dbn.debugger.common.config.DBStatementRunConfig;
import com.dbn.debugger.common.config.ui.DBStatementRunConfigEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DBStatementJdwpRunConfig extends DBStatementRunConfig implements DBJdwpRunConfig{

    public DBStatementJdwpRunConfig(Project project, DBStatementJdwpRunConfigFactory factory, String name, DBRunConfigCategory category) {
        super(project, factory, name, category);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DBStatementRunConfigEditor(this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBStatementJdwpRunProfileState(env);
    }

    @Override
    public boolean canRun() {
        return super.canRun() && DBDebuggerType.JDWP.isSupported();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        DatabaseDebuggerManager.checkJdwpConfiguration();
        super.checkConfiguration();
    }

}
