package com.dci.intellij.dbn.editor.data.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.editor.data.options.DataEditorPopupSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DataEditorPopupSettingsForm extends ConfigurationEditorForm<DataEditorPopupSettings> {
    private JTextField lengthThresholdTextField;
    private JTextField delayTextField;
    private JCheckBox activeCheckBox;
    private JCheckBox activeIfEmptyCheckBox;
    private JPanel mainPanel;

    public DataEditorPopupSettingsForm(DataEditorPopupSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetChanges();
        enableDisableFields();

        registerComponent(lengthThresholdTextField);
        registerComponent(delayTextField);
        registerComponent(activeCheckBox);
        registerComponent(activeIfEmptyCheckBox);
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                if (e.getSource() == activeCheckBox) {
                    enableDisableFields();
                }
            }
        };
    }

    private void enableDisableFields() {
        boolean enabled = activeCheckBox.isSelected();
        activeIfEmptyCheckBox.setEnabled(enabled);
        lengthThresholdTextField.setEnabled(enabled);
        delayTextField.setEnabled(enabled);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DataEditorPopupSettings settings = getConfiguration();
        settings.setActive(activeCheckBox.isSelected());
        settings.setActiveIfEmpty(activeIfEmptyCheckBox.isSelected());
        if (settings.isActive()) {
            settings.setDataLengthThreshold(ConfigurationEditorUtil.validateIntegerInputValue(lengthThresholdTextField, "Length threshold", 0, 999999999, null));
            settings.setDelay(ConfigurationEditorUtil.validateIntegerInputValue(delayTextField, "Delay", 10, 2000, null));
        }
    }

    public void resetChanges() {
        DataEditorPopupSettings settings = getConfiguration();
        activeCheckBox.setSelected(settings.isActive());
        activeIfEmptyCheckBox.setSelected(settings.isActiveIfEmpty());
        lengthThresholdTextField.setText(Integer.toString(settings.getDataLengthThreshold()));
        delayTextField.setText(Integer.toString(settings.getDelay()));
    }
}
