package com.dci.intellij.dbn.editor.data.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.data.options.DataEditorPopupSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;

import static com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil.validateIntegerInputValue;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DataEditorPopupSettingsForm extends ConfigurationEditorForm<DataEditorPopupSettings> {
    private JTextField lengthThresholdTextField;
    private JTextField delayTextField;
    private JCheckBox activeCheckBox;
    private JCheckBox activeIfEmptyCheckBox;
    private JPanel mainPanel;

    public DataEditorPopupSettingsForm(DataEditorPopupSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        enableDisableFields();

        registerComponent(mainPanel);
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            if (e.getSource() == activeCheckBox) {
                enableDisableFields();
            }
        };
    }

    private void enableDisableFields() {
        boolean enabled = activeCheckBox.isSelected();
        activeIfEmptyCheckBox.setEnabled(enabled);
        lengthThresholdTextField.setEnabled(enabled);
        delayTextField.setEnabled(enabled);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataEditorPopupSettings settings = getConfiguration();
        settings.setActive(activeCheckBox.isSelected());
        settings.setActiveIfEmpty(activeIfEmptyCheckBox.isSelected());
        if (settings.isActive()) {
            settings.setDataLengthThreshold(validateIntegerInputValue(lengthThresholdTextField, "Length threshold", true, 0, 999999999, null));
            settings.setDelay(validateIntegerInputValue(delayTextField, "Delay", true, 10, 2000, null));
        }
    }

    @Override
    public void resetFormChanges() {
        DataEditorPopupSettings settings = getConfiguration();
        activeCheckBox.setSelected(settings.isActive());
        activeIfEmptyCheckBox.setSelected(settings.isActiveIfEmpty());
        lengthThresholdTextField.setText(Integer.toString(settings.getDataLengthThreshold()));
        delayTextField.setText(Integer.toString(settings.getDelay()));
    }
}
