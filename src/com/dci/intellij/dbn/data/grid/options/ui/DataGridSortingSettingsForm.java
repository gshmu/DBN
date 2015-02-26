package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.ui.TextPresentable;
import com.dci.intellij.dbn.data.grid.options.DataGridSortingSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public class DataGridSortingSettingsForm extends ConfigurationEditorForm<DataGridSortingSettings> {
    private JPanel mainPanel;
    private JRadioButton nullsFirstRadioButton;
    private JRadioButton nullsLastRadioButton;
    private JTextField maxSortingColumnsTextField;
    private JPanel nullValuesPositionPanel;
    private DBNComboBox nullValuesPositionComboBox;

    public DataGridSortingSettingsForm(DataGridSortingSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);

        List<Presentable> options = new ArrayList<Presentable>();
        options.add(new TextPresentable("FIRST"));
        options.add(new TextPresentable("LAST"));
        nullValuesPositionComboBox = new DBNComboBox<>(options);
        nullValuesPositionPanel.add(nullValuesPositionComboBox, BorderLayout.CENTER);
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
