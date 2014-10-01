package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.options.DataEditorFilterSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class DataEditorFilterSettingsForm extends ConfigurationEditorForm<DataEditorFilterSettings> {
    private JPanel mainPanel;
    private JCheckBox promptFilterDialogCheckBox;
    private JComboBox defaultFilterTypeComboBox;

    public DataEditorFilterSettingsForm(DataEditorFilterSettings settings) {
        super(settings);
        defaultFilterTypeComboBox.addItem(DatasetFilterType.NONE);
        defaultFilterTypeComboBox.addItem(DatasetFilterType.BASIC);
        defaultFilterTypeComboBox.addItem(DatasetFilterType.CUSTOM);
        defaultFilterTypeComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                DatasetFilterType filterType = (DatasetFilterType) value;
                setIcon(filterType.getIcon());
                append(filterType.name(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
        registerComponent(mainPanel);
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
            }
        };
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataEditorFilterSettings settings = getConfiguration();
        settings.setPromptFilterDialog(promptFilterDialogCheckBox.isSelected());
        settings.setDefaultFilterType((DatasetFilterType) defaultFilterTypeComboBox.getSelectedItem());
    }


    public void resetFormChanges() {
        DataEditorFilterSettings settings = getConfiguration();
        promptFilterDialogCheckBox.setSelected(settings.isPromptFilterDialog());
        defaultFilterTypeComboBox.setSelectedItem(settings.getDefaultFilterType());
    }
}
