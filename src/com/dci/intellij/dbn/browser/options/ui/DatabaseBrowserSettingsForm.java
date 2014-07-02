package com.dci.intellij.dbn.browser.options.ui;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;

import javax.swing.*;
import java.awt.*;


public class DatabaseBrowserSettingsForm extends CompositeConfigurationEditorForm<DatabaseBrowserSettings> {
    private JPanel mainPanel;
    private JPanel generalSettingsPanel;
    private JPanel filterSettingsPanel;

    public DatabaseBrowserSettingsForm(DatabaseBrowserSettings settings) {
        super(settings);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        filterSettingsPanel.add(settings.getFilterSettings().createComponent(), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
