package com.dci.intellij.dbn.code.common.completion.options.ui;

import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;

import javax.swing.*;
import java.awt.*;

public class CodeCompletionSettingsForm extends CompositeConfigurationEditorForm<CodeCompletionSettings> {
    private JPanel mainPanel;
    private JPanel filterPanel;
    private JPanel sortingPanel;

    public CodeCompletionSettingsForm(CodeCompletionSettings codeCompletionSettings) {
        super(codeCompletionSettings);

        filterPanel.add(codeCompletionSettings.getFilterSettings().createComponent(), BorderLayout.CENTER);
        sortingPanel.add(codeCompletionSettings.getSortingSettings().createComponent(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
