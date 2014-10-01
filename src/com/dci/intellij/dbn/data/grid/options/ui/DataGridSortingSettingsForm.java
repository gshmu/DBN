package com.dci.intellij.dbn.data.grid.options.ui;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.data.grid.options.DataGridSortingSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataGridSortingSettingsForm extends ConfigurationEditorForm<DataGridSortingSettings> {
    private JPanel mainPanel;
    private JRadioButton nullsFirstRadioButton;
    private JRadioButton nullsLastRadioButton;
    private JTextField maxSortingColumnsTextField;

    public DataGridSortingSettingsForm(DataGridSortingSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataGridSortingSettings settings = getConfiguration();
        settings.setNullsFirst(nullsFirstRadioButton.isSelected());
        int maxSortingColumns = ConfigurationEditorUtil.validateIntegerInputValue(maxSortingColumnsTextField, "Max sorting columns", 0, 100, "Use value 0 for unlimited number of sorting columns");
        settings.setMaxSortingColumns(maxSortingColumns);
    }

    public void resetFormChanges() {
        DataGridSortingSettings settings = getConfiguration();
        nullsFirstRadioButton.setSelected(settings.isNullsFirst());
        nullsLastRadioButton.setSelected(!settings.isNullsFirst());
        maxSortingColumnsTextField.setText(Integer.toString(settings.getMaxSortingColumns()));
    }
}
