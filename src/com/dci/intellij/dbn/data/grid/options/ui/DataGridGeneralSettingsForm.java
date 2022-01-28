package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class DataGridGeneralSettingsForm extends ConfigurationEditorForm<DataGridGeneralSettings> {
    private JPanel mainPanel;
    private JCheckBox enableZoomingCheckBox;
    private JCheckBox enableColumnTooltipsCheckBox;

    public DataGridGeneralSettingsForm(DataGridGeneralSettings settings) {
        super(settings);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataGridGeneralSettings settings = getConfiguration();
        settings.setZoomingEnabled(enableZoomingCheckBox.isSelected());
        settings.setColumnTooltipEnabled(enableColumnTooltipsCheckBox.isSelected());
    }

    @Override
    public void resetFormChanges() {
        DataGridGeneralSettings settings = getConfiguration();
        enableZoomingCheckBox.setSelected(settings.isZoomingEnabled());
        enableColumnTooltipsCheckBox.setSelected(settings.isColumnTooltipEnabled());
    }
}
