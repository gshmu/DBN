package com.dci.intellij.dbn.data.grid.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.list.EditableStringListForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettingsChangeListener;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataGridTrackingColumnSettingsForm extends ConfigurationEditorForm<DataGridTrackingColumnSettings> {
    private JPanel mainPanel;
    private JCheckBox visibleCheckBox;
    private JCheckBox editableCheckBox;
    private JPanel columnNameListPanel;

    private EditableStringListForm editableStringListForm;

    public DataGridTrackingColumnSettingsForm(DataGridTrackingColumnSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        editableStringListForm = new EditableStringListForm("Tracking column names", true);
        JComponent listComponent = editableStringListForm.getComponent();
        columnNameListPanel.add(listComponent, BorderLayout.CENTER);

        resetChanges();
        editableCheckBox.setEnabled(visibleCheckBox.isSelected());
        registerComponent(mainPanel);
        //registerComponent(visibleCheckBox);
        //registerComponent(editableCheckBox);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                if (e.getSource() == visibleCheckBox) {
                    editableCheckBox.setEnabled(visibleCheckBox.isSelected());
                }
            }
        };
    }

    public void applyChanges() throws ConfigurationException {
        DataGridTrackingColumnSettings settings = getConfiguration();
        boolean trackingColumnsVisible = visibleCheckBox.isSelected();
        boolean visibilityChanged = settings.isShowColumns() != trackingColumnsVisible;
        settings.setShowColumns(trackingColumnsVisible);
        settings.setAllowEditing(editableCheckBox.isSelected());

        Set<String> columnNamesSet = settings.getColumnNames();
        columnNamesSet.clear();
        columnNamesSet.addAll(editableStringListForm.getStringValues());

        if (visibilityChanged) {
            EventManager.notify(settings.getProject(), DataGridSettingsChangeListener.TOPIC).trackingColumnsVisibilityChanged(trackingColumnsVisible);
        }
    }

    public void resetChanges() {
        DataGridTrackingColumnSettings settings = getConfiguration();
        visibleCheckBox.setSelected(settings.isShowColumns());
        editableCheckBox.setSelected(settings.isAllowEditing());
        StringBuilder buffer = new StringBuilder();
        Set<String> columnNamesSet = settings.getColumnNames();
        editableStringListForm.setStringValues(columnNamesSet);
    }
}
