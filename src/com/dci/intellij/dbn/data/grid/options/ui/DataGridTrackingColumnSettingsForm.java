package com.dci.intellij.dbn.data.grid.options.ui;

import javax.swing.*;
import java.util.Set;
import java.util.StringTokenizer;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataGridTrackingColumnSettingsForm extends ConfigurationEditorForm<DataGridTrackingColumnSettings> {
    private JTextField columnNamesTextField;
    private JPanel mainPanel;
    private JCheckBox visibleCheckBox;
    private JCheckBox editableCheckBox;

    public DataGridTrackingColumnSettingsForm(DataGridTrackingColumnSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetChanges();

        registerComponent(columnNamesTextField);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DataGridTrackingColumnSettings settings = getConfiguration();
        settings.setVisible(visibleCheckBox.isSelected());
        settings.setEditable(editableCheckBox.isSelected());
        StringTokenizer columnNames = new StringTokenizer(columnNamesTextField.getText(), ",");
        Set<String> columnNamesSet = settings.getColumnNames();
        columnNamesSet.clear();
        while (columnNames.hasMoreTokens()) {
            String columnName = columnNames.nextToken().trim().toUpperCase();
            columnNamesSet.add(columnName);
        }
    }

    public void resetChanges() {
        DataGridTrackingColumnSettings settings = getConfiguration();
        visibleCheckBox.setSelected(settings.isVisible());
        editableCheckBox.setSelected(settings.isEditable());
        StringBuilder buffer = new StringBuilder();
        Set<String> columnNamesSet = settings.getColumnNames();
        for (String columnName : columnNamesSet) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(columnName);
        }
        columnNamesTextField.setText(buffer.toString());
    }
}
