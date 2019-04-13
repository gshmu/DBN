package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.list.EditableStringListForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.data.grid.options.DataGridSettingsChangeListener;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public JPanel ensureComponent() {
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

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataGridTrackingColumnSettings configuration = getConfiguration();
        boolean trackingColumnsVisible = visibleCheckBox.isSelected();
        boolean visibilityChanged = configuration.isShowColumns() != trackingColumnsVisible;
        configuration.setShowColumns(trackingColumnsVisible);
        configuration.setAllowEditing(editableCheckBox.isSelected());

        configuration.setColumnNames(editableStringListForm.getStringValues());

        Project project = configuration.getProject();
        SettingsChangeNotifier.register(() -> {
            if (visibilityChanged) {
                EventUtil.notify(project,
                        DataGridSettingsChangeListener.TOPIC,
                        (listener) -> listener.trackingColumnsVisibilityChanged(trackingColumnsVisible));
            }
        });
    }

    @Override
    public void resetFormChanges() {
        DataGridTrackingColumnSettings settings = getConfiguration();
        visibleCheckBox.setSelected(settings.isShowColumns());
        editableCheckBox.setSelected(settings.isAllowEditing());
        Collection<String> columnNamesSet = settings.getColumnNames();
        editableStringListForm.setStringValues(columnNamesSet);
    }
}
