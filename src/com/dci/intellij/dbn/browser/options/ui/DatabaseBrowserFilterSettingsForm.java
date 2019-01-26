package com.dci.intellij.dbn.browser.options.ui;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserFilterSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DatabaseBrowserFilterSettingsForm extends CompositeConfigurationEditorForm<DatabaseBrowserFilterSettings> {
    private JPanel mainPanel;
    private JPanel visibleObjectTypesPanel;

    public DatabaseBrowserFilterSettingsForm(DatabaseBrowserFilterSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        visibleObjectTypesPanel.add(settings.getObjectTypeFilterSettings().createComponent(), BorderLayout.CENTER);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
