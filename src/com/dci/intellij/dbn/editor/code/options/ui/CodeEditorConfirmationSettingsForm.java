package com.dci.intellij.dbn.editor.code.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeEditorConfirmationSettingsForm extends ConfigurationEditorForm<CodeEditorConfirmationSettings> {
    private JPanel mainPanel;
    private JCheckBox confirmSaveCheckBox;
    private JCheckBox confirmRevertCheckBox;
    private DBNComboBox<CodeEditorChangesOption> disconnectSessionComboBox;

    public CodeEditorConfirmationSettingsForm(CodeEditorConfirmationSettings settings) {
        super(settings);

        disconnectSessionComboBox.setValues(CodeEditorChangesOption.ASK, CodeEditorChangesOption.SAVE, CodeEditorChangesOption.DISCARD);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeEditorConfirmationSettings settings = getConfiguration();
        settings.getSaveChanges().setConfirm(confirmSaveCheckBox.isSelected());
        settings.getRevertChanges().setConfirm(confirmRevertCheckBox.isSelected());
        settings.getExitOnChanges().set(disconnectSessionComboBox.getSelectedValue());
    }

    public void resetFormChanges() {
        CodeEditorConfirmationSettings settings = getConfiguration();
        confirmSaveCheckBox.setSelected(settings.getSaveChanges().isConfirm());
        confirmRevertCheckBox.setSelected(settings.getRevertChanges().isConfirm());
        disconnectSessionComboBox.setSelectedValue(settings.getExitOnChanges().get());
    }
}
