package com.dci.intellij.dbn.data.grid.options.ui;

import javax.swing.*;
import java.awt.*;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;

public class DataGridSettingsForm extends CompositeConfigurationEditorForm<DataGridSettings> {
    private JPanel mainPanel;
    private JPanel trackingColumnSettingsPanel;

    public DataGridSettingsForm(DataGridSettings settings) {
        super(settings);
        trackingColumnSettingsPanel.add(settings.getTrackingColumnSettings().createComponent(), BorderLayout.CENTER);
        resetChanges();
    }


    public JPanel getComponent() {
        return mainPanel;
    }
}
