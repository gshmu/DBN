package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.editor.data.options.DataEditorValueListPopupSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DatatEditorValueListPopupSettingsForm extends ConfigurationEditorForm<DataEditorValueListPopupSettings> {
    private JTextField elementCountThresholdTextBox;
    private JTextField dataLengthThresholdTextBox;
    private JCheckBox activeForPrimaryKeysCheckBox;
    private JPanel mainPanel;

    public DatatEditorValueListPopupSettingsForm(DataEditorValueListPopupSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DataEditorValueListPopupSettings settings = getConfiguration();
        settings.setActiveForPrimaryKeyColumns(activeForPrimaryKeysCheckBox.isSelected());
        settings.setElementCountThreshold(ConfigurationEditorUtil.validateIntegerInputValue(elementCountThresholdTextBox, "Element count threshold", 0, 10000, null));
        settings.setDataLengthThreshold(ConfigurationEditorUtil.validateIntegerInputValue(dataLengthThresholdTextBox, "Data length threshold", 0, 1000, null));
    }

    public void resetChanges() {
        DataEditorValueListPopupSettings settings = getConfiguration();
        activeForPrimaryKeysCheckBox.setSelected(settings.isActiveForPrimaryKeyColumns());
        elementCountThresholdTextBox.setText(Integer.toString(settings.getElementCountThreshold()));
        dataLengthThresholdTextBox.setText(Integer.toString(settings.getDataLengthThreshold()));
    }
}
