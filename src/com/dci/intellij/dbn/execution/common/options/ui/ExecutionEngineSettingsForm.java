package com.dci.intellij.dbn.execution.common.options.ui;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ExecutionEngineSettingsForm extends CompositeConfigurationEditorForm<ExecutionEngineSettings> {
    private JPanel mainPanel;
    private JPanel statementExecutionPanel;
    private JPanel methodExecutionPanel;
    private JPanel scriptExecutionPanel;

    public ExecutionEngineSettingsForm(ExecutionEngineSettings settings) {
        super(settings);
        statementExecutionPanel.add(settings.getStatementExecutionSettings().createComponent(), BorderLayout.CENTER);
        methodExecutionPanel.add(settings.getMethodExecutionSettings().createComponent(), BorderLayout.CENTER);
        scriptExecutionPanel.add(settings.getScriptExecutionSettings().createComponent(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
