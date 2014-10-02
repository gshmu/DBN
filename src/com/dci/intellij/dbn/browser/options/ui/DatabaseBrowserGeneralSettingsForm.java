package com.dci.intellij.dbn.browser.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserGeneralSettings;
import com.dci.intellij.dbn.browser.options.listener.DisplayModeSettingsListener;
import com.dci.intellij.dbn.browser.options.listener.ObjectDetailSettingsListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
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
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        ConfigurationEditorUtil.validateIntegerInputValue(navigationHistorySizeTextField, "Navigation history size", 0, 1000, "");

        final boolean repaintTree = configuration.isModified();
        
        final BrowserDisplayMode displayMode =
                simpleRadioButton.isSelected() ? BrowserDisplayMode.SIMPLE :
                tabbedRadioButton.isSelected() ? BrowserDisplayMode.TABBED :
                BrowserDisplayMode.SIMPLE;
        final boolean displayModeChanged = configuration.getDisplayMode() != displayMode;
        configuration.setDisplayMode(displayMode);


        configuration.getNavigationHistorySize().applyChanges(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().applyChanges(showObjectDetailsCheckBox);

        final Project project = configuration.getProject();

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (displayModeChanged) {
                    DisplayModeSettingsListener listener = EventManager.notify(project, DisplayModeSettingsListener.TOPIC);
                    listener.displayModeChanged(displayMode);
                } else if (repaintTree) {
                    ObjectDetailSettingsListener listener = EventManager.notify(project, ObjectDetailSettingsListener.TOPIC);
                    listener.displayDetailsChanged();
                }
            }
        };
    }

    public void resetFormChanges() {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        BrowserDisplayMode displayMode = configuration.getDisplayMode();
        if (displayMode == BrowserDisplayMode.SIMPLE) simpleRadioButton.setSelected(true); else
        if (displayMode == BrowserDisplayMode.TABBED) tabbedRadioButton.setSelected(true);

        configuration.getNavigationHistorySize().resetChanges(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().resetChanges(showObjectDetailsCheckBox);
    }
}
