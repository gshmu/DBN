package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.*;
import java.awt.*;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.connection.config.ConnectionFilterSettings;

public class ConnectionFilterSettingsForm extends CompositeConfigurationEditorForm<ConnectionFilterSettings>{
    private JPanel mainPanel;
    private JPanel objectTypesFilterPanel;
    private JPanel objectNameFiltersPanel;

    public ConnectionFilterSettingsForm(ConnectionFilterSettings settings) {
        super(settings);
        objectTypesFilterPanel.add(settings.getObjectTypeFilterSettings().createComponent(), BorderLayout.CENTER);
        objectNameFiltersPanel.add(settings.getObjectNameFilterSettings().createComponent(), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
