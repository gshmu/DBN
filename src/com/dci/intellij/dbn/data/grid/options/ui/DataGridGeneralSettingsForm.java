package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DataGridGeneralSettingsForm extends ConfigurationEditorForm<DataGridGeneralSettings> {
    private JPanel mainPanel;
    private JCheckBox enableZoomingCheckBox;
    private JCheckBox enableColumnTooltipsCheckBox;

    public DataGridGeneralSettingsForm(DataGridGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
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
