package com.dci.intellij.dbn.browser.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserGeneralSettings;
import com.dci.intellij.dbn.browser.options.listener.DisplayModeSettingsListener;
import com.dci.intellij.dbn.browser.options.listener.ObjectDetailSettingsListener;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DatabaseBrowserGeneralSettingsForm extends ConfigurationEditorForm<DatabaseBrowserGeneralSettings> {
    private JPanel mainPanel;
    private JTextField navigationHistorySizeTextField;
    private JCheckBox showObjectDetailsCheckBox;
    private DBNComboBox<BrowserDisplayMode> browserTypeComboBox;


    public DatabaseBrowserGeneralSettingsForm(DatabaseBrowserGeneralSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);

        browserTypeComboBox.setValues(
                BrowserDisplayMode.SIMPLE,
                BrowserDisplayMode.TABBED);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        ConfigurationEditorUtil.validateIntegerInputValue(navigationHistorySizeTextField, "Navigation history size", true, 0, 1000, "");

        final boolean repaintTree = configuration.isModified();
        
        final BrowserDisplayMode displayMode = browserTypeComboBox.getSelectedValue();
        final boolean displayModeChanged = configuration.getDisplayMode() != displayMode;
        configuration.setDisplayMode(displayMode);


        configuration.getNavigationHistorySize().to(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().to(showObjectDetailsCheckBox);

        final Project project = configuration.getProject();

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (displayModeChanged) {
                    DisplayModeSettingsListener listener = EventUtil.notify(project, DisplayModeSettingsListener.TOPIC);
                    listener.displayModeChanged(displayMode);
                } else if (repaintTree) {
                    ObjectDetailSettingsListener listener = EventUtil.notify(project, ObjectDetailSettingsListener.TOPIC);
                    listener.displayDetailsChanged();
                }
            }
        };
    }

    public void resetFormChanges() {
        DatabaseBrowserGeneralSettings configuration = getConfiguration();
        browserTypeComboBox.setSelectedValue(configuration.getDisplayMode());

        configuration.getNavigationHistorySize().from(navigationHistorySizeTextField);
        configuration.getShowObjectDetails().from(showObjectDetailsCheckBox);
    }

}
