package com.dci.intellij.dbn.editor.code.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.code.options.CodeEditorChangesOption;
import com.dci.intellij.dbn.editor.code.options.CodeEditorConfirmationSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.getSelection;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.initComboBox;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.setSelection;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeEditorConfirmationSettingsForm extends ConfigurationEditorForm<CodeEditorConfirmationSettings> {
    private JPanel mainPanel;
    private JCheckBox confirmSaveCheckBox;
    private JCheckBox confirmRevertCheckBox;
    private JComboBox<CodeEditorChangesOption> disconnectSessionComboBox;

    public CodeEditorConfirmationSettingsForm(CodeEditorConfirmationSettings settings) {
        super(settings);

        initComboBox(disconnectSessionComboBox,
                CodeEditorChangesOption.ASK,
                CodeEditorChangesOption.SAVE,
                CodeEditorChangesOption.DISCARD);

        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        CodeEditorConfirmationSettings settings = getConfiguration();
        settings.getSaveChanges().setConfirm(confirmSaveCheckBox.isSelected());
        settings.getRevertChanges().setConfirm(confirmRevertCheckBox.isSelected());
        settings.getExitOnChanges().set(getSelection(disconnectSessionComboBox));
    }

    @Override
    public void resetFormChanges() {
        CodeEditorConfirmationSettings settings = getConfiguration();
        confirmSaveCheckBox.setSelected(settings.getSaveChanges().isConfirm());
        confirmRevertCheckBox.setSelected(settings.getRevertChanges().isConfirm());
        setSelection(disconnectSessionComboBox, settings.getExitOnChanges().get());
    }
}
