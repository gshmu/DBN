package com.dci.intellij.dbn.data.grid.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;

public class DataGridSettingsForm extends CompositeConfigurationEditorForm<DataGridSettings> {
    private JPanel mainPanel;
    private JPanel trackingColumnSettingsPanel;
    private JPanel sortingSettingsPanel;

    public DataGridSettingsForm(DataGridSettings settings) {
        super(settings);
        trackingColumnSettingsPanel.add(settings.getTrackingColumnSettings().createComponent(), BorderLayout.CENTER);
        sortingSettingsPanel.add(settings.getSortingSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    public JPanel getComponent() {
        return mainPanel;
    }
}
