package com.dci.intellij.dbn.execution.statement.options;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.statement.options.ui.StatementExecutionSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class StatementExecutionSettings extends BasicProjectConfiguration<ExecutionEngineSettings, ConfigurationEditorForm> implements ExecutionTimeoutSettings, ProjectSupplier {
    private static final String REMEMBER_OPTION_HINT = ""/*"\n\n(you can remember your option and change it at any time in Settings > Execution Engine > Statement Execution)"*/;

    private int resultSetFetchBlockSize = 100;
    private int executionTimeout = 20;
    private int debugExecutionTimeout = 600;
    private boolean focusResult = false;
    private boolean promptExecution = false;

    public StatementExecutionSettings(ExecutionEngineSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Statement execution settings";
    }

    @Override
    public String getHelpTopic() {
        return "executionEngine";
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public ConfigurationEditorForm createConfigurationEditor() {
        return new StatementExecutionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "statement-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        resultSetFetchBlockSize = SettingsSupport.getInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        executionTimeout = SettingsSupport.getInteger(element, "execution-timeout", executionTimeout);
        debugExecutionTimeout = SettingsSupport.getInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        focusResult = SettingsSupport.getBoolean(element, "focus-result", focusResult);
        promptExecution = SettingsSupport.getBoolean(element, "prompt-execution", promptExecution);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setInteger(element, "fetch-block-size", resultSetFetchBlockSize);
        SettingsSupport.setInteger(element, "execution-timeout", executionTimeout);
        SettingsSupport.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        SettingsSupport.setBoolean(element, "focus-result", focusResult);
        SettingsSupport.setBoolean(element, "prompt-execution", promptExecution);
    }
}
