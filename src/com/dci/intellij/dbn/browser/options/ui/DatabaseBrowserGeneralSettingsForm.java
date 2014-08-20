package com.dci.intellij.dbn.browser.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserGeneralSettings;
import com.dci.intellij.dbn.browser.options.ObjectDisplaySettingsListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class DatabaseBrowserGeneralSettingsForm extends ConfigurationEditorForm<DatabaseBrowserGeneralSettings> {
    private JPanel mainPanel;
    private JRadioButton simpleRadioButton;
    private JRadioButton tabbedRadioButton;
    private JTextField navigationHistorySizeTextField;
    private JCheckBox showObjectDetailsCheckBox;


    public DatabaseBrowserGeneralSettingsForm(DatabaseBrowserGeneralSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);
        resetChanges();

        registerComponent(mainPanel);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        boolean repaintTree = configuration.isModified();
        
        BrowserDisplayMode displayMode =
                simpleRadioButton.isSelected() ? BrowserDisplayMode.SIMPLE :
                tabbedRadioButton.isSelected() ? BrowserDisplayMode.TABBED :
                BrowserDisplayMode.SIMPLE;
        configuration.setDisplayMode(displayMode);

        ConfigurationEditorUtil.validateIntegerInputValue(navigationHistorySizeTextField, "Navigation history size", 0, 1000, "");
        configuration.getNavigationHistorySize().applyChanges(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().applyChanges(showObjectDetailsCheckBox);
        
        if (repaintTree) {
            Project project = configuration.getProject();
            ObjectDisplaySettingsListener listener = EventManager.notify(project, ObjectDisplaySettingsListener.TOPIC);
            listener.displayDetailsChanged();
        }
        
    }

    public void resetChanges() {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        BrowserDisplayMode displayMode = configuration.getDisplayMode();
        if (displayMode == BrowserDisplayMode.SIMPLE) simpleRadioButton.setSelected(true); else
        if (displayMode == BrowserDisplayMode.TABBED) tabbedRadioButton.setSelected(true);

        configuration.getNavigationHistorySize().resetChanges(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().resetChanges(showObjectDetailsCheckBox);
    }
}
