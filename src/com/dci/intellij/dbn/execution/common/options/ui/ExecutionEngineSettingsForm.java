package com.dci.intellij.dbn.execution.common.options.ui;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;

import javax.swing.*;
import java.awt.*;

public class ExecutionEngineSettingsForm extends CompositeConfigurationEditorForm<ExecutionEngineSettings> {
    private JPanel mainPanel;
    private JPanel queryExecutionPanel;
    private JPanel compilerPanel;

    public ExecutionEngineSettingsForm(ExecutionEngineSettings settings) {
        super(settings);
        queryExecutionPanel.add(settings.getStatementExecutionSettings().createComponent(), BorderLayout.CENTER);
        compilerPanel.add(settings.getCompilerSettings().createComponent(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
