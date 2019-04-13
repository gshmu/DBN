package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DataGridSettingsForm extends CompositeConfigurationEditorForm<DataGridSettings> {
    private JPanel mainPanel;
    private JPanel trackingColumnSettingsPanel;
    private JPanel sortingSettingsPanel;
    private JPanel generalSettingsPanel;

    public DataGridSettingsForm(DataGridSettings settings) {
        super(settings);
        trackingColumnSettingsPanel.add(settings.getTrackingColumnSettings().createComponent(), BorderLayout.CENTER);
        sortingSettingsPanel.add(settings.getSortingSettings().createComponent(), BorderLayout.CENTER);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }
}
