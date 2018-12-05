package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.list.EditableStringListForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.data.grid.options.DataGridSettingsChangeListener;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DataGridTrackingColumnSettingsForm extends ConfigurationEditorForm<DataGridTrackingColumnSettings> {
    private JPanel mainPanel;
    private JCheckBox visibleCheckBox;
    private JCheckBox editableCheckBox;
    private JPanel columnNameListPanel;

    private EditableStringListForm editableStringListForm;

    public DataGridTrackingColumnSettingsForm(DataGridTrackingColumnSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        editableStringListForm = new EditableStringListForm(this, "Tracking column names", true);
        JComponent listComponent = editableStringListForm.getComponent();
        columnNameListPanel.add(listComponent, BorderLayout.CENTER);

        resetFormChanges();
        editableCheckBox.setEnabled(visibleCheckBox.isSelected());
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            if (e.getSource() == visibleCheckBox) {
                editableCheckBox.setEnabled(visibleCheckBox.isSelected());
            }
        };
    }

    public void applyFormChanges() throws ConfigurationException {
        final DataGridTrackingColumnSettings settings = getConfiguration();
        final boolean trackingColumnsVisible = visibleCheckBox.isSelected();
        final boolean visibilityChanged = settings.isShowColumns() != trackingColumnsVisible;
        settings.setShowColumns(trackingColumnsVisible);
        settings.setAllowEditing(editableCheckBox.isSelected());

        settings.setColumnNames(editableStringListForm.getStringValues());

        SettingsChangeNotifier.register(() -> {
            if (visibilityChanged) {
                DataGridSettingsChangeListener listener = EventUtil.notify(settings.getProject(), DataGridSettingsChangeListener.TOPIC);
                listener.trackingColumnsVisibilityChanged(trackingColumnsVisible);
            }
        });
    }

    public void resetFormChanges() {
        DataGridTrackingColumnSettings settings = getConfiguration();
        visibleCheckBox.setSelected(settings.isShowColumns());
        editableCheckBox.setSelected(settings.isAllowEditing());
        Collection<String> columnNamesSet = settings.getColumnNames();
        editableStringListForm.setStringValues(columnNamesSet);
    }
}
