package com.dci.intellij.dbn.browser.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserFilterSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DatabaseBrowserFilterSettingsForm extends CompositeConfigurationEditorForm<DatabaseBrowserFilterSettings> {
    private JPanel mainPanel;
    private JPanel visibleObjectTypesPanel;

    public DatabaseBrowserFilterSettingsForm(DatabaseBrowserFilterSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        visibleObjectTypesPanel.add(settings.getObjectTypeFilterSettings().createComponent(), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
