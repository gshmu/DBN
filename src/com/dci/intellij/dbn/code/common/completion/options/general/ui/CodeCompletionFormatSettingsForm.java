package com.dci.intellij.dbn.code.common.completion.options.general.ui;

import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeCompletionFormatSettingsForm extends ConfigurationEditorForm<CodeCompletionFormatSettings> {
    private JCheckBox enforceCaseCheckBox;
    private JPanel mainPanel;

    public CodeCompletionFormatSettingsForm(CodeCompletionFormatSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        CodeCompletionFormatSettings settings = getConfiguration();
        settings.setEnforceCodeStyleCase(enforceCaseCheckBox.isSelected());
    }

    @Override
    public void resetFormChanges() {
        CodeCompletionFormatSettings settings = getConfiguration();
        enforceCaseCheckBox.setSelected(settings.isEnforceCodeStyleCase());
    }
}
