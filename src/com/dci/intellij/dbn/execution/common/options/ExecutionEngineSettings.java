package com.dci.intellij.dbn.execution.common.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.execution.common.options.ui.ExecutionEngineSettingsForm;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.dci.intellij.dbn.execution.script.options.ScriptExecutionSettings;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class ExecutionEngineSettings extends CompositeProjectConfiguration<ExecutionEngineSettingsForm> implements TopLevelConfig {
    private StatementExecutionSettings statementExecutionSettings = new StatementExecutionSettings();
    private ScriptExecutionSettings scriptExecutionSettings = new ScriptExecutionSettings(this);
    private MethodExecutionSettings methodExecutionSettings = new MethodExecutionSettings();
    private CompilerSettings compilerSettings = new CompilerSettings();

    public ExecutionEngineSettings(Project project) {
        super(project);
    }

    public static ExecutionEngineSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getExecutionEngineSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.ExecutionEngineSettings";
    }

    public String getDisplayName() {
        return "Execution Engine";
    }

    public String getHelpTopic() {
        return "executionEngine";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.EXECUTION_ENGINE;
    }

    @Override
    protected Configuration<ExecutionEngineSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/
    public StatementExecutionSettings getStatementExecutionSettings() {
        return statementExecutionSettings;
    }

    public ScriptExecutionSettings getScriptExecutionSettings() {
        return scriptExecutionSettings;
    }

    public MethodExecutionSettings getMethodExecutionSettings() {
        return methodExecutionSettings;
    }

    public CompilerSettings getCompilerSettings() {
        return compilerSettings;
    }

    /*********************************************************
     *                    Configuration                      *
     *********************************************************/
    @NotNull
    public ExecutionEngineSettingsForm createConfigurationEditor() {
        return new ExecutionEngineSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "execution-engine-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                statementExecutionSettings,
                scriptExecutionSettings,
                methodExecutionSettings,
                compilerSettings};
    }
}
