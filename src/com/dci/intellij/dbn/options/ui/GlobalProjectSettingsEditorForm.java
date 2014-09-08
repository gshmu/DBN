package com.dci.intellij.dbn.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GlobalConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.GlobalConnectionSettingsForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.GlobalProjectSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;

public class GlobalProjectSettingsEditorForm extends CompositeConfigurationEditorForm<GlobalProjectSettings> {
    private JPanel mainPanel;
    private TabbedPane configurationTabs;

    public GlobalProjectSettingsEditorForm(GlobalProjectSettings globalSettings) {
        super(globalSettings);

        configurationTabs = new TabbedPane(globalSettings.getProject());
        //configurationTabs.setAdjustBorders(false);

        mainPanel.add(configurationTabs, BorderLayout.CENTER);

        GlobalConnectionSettings connectionSettings = globalSettings.getConnectionSettings();
        DatabaseBrowserSettings browserSettings = globalSettings.getBrowserSettings();
        NavigationSettings navigationSettings = globalSettings.getNavigationSettings();
        CodeCompletionSettings codeCompletionSettings = globalSettings.getCodeCompletionSettings();
        DataGridSettings dataGridSettings = globalSettings.getDataGridSettings();
        DataEditorSettings dataEditorSettings = globalSettings.getDataEditorSettings();
        ExecutionEngineSettings executionEngineSettings = globalSettings.getExecutionEngineSettings();
        DDLFileSettings ddlFileSettings = globalSettings.getDdlFileSettings();
        GeneralProjectSettings generalSettings = globalSettings.getGeneralSettings();

        addSettingsPanel(connectionSettings);
        addSettingsPanel(browserSettings);
        addSettingsPanel(navigationSettings);
        addSettingsPanel(codeCompletionSettings);
        addSettingsPanel(dataGridSettings);
        addSettingsPanel(dataEditorSettings);
        addSettingsPanel(executionEngineSettings);
        addSettingsPanel(ddlFileSettings);
        addSettingsPanel(generalSettings);
        mainPanel.setFocusable(false);
    }

    private void addSettingsPanel(Configuration configuration) {
        JComponent component = configuration.createComponent();
        JBScrollPane scrollPane = new JBScrollPane(component);
        TabInfo tabInfo = new TabInfo(scrollPane);
        tabInfo.setText(configuration.getDisplayName());
        tabInfo.setObject(configuration);
        //tabInfo.setTabColor(GUIUtil.getWindowColor());
        configurationTabs.addTab(tabInfo);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void focusConnectionSettings(ConnectionHandler connectionHandler) {
        GlobalConnectionSettings connectionSettings = getConfiguration().getConnectionSettings();
        GlobalConnectionSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.focusConnectionSettings(connectionHandler);
            focusSettingsEditor(connectionSettings);
        }
    }

    public void focusSettingsEditor(Configuration configuration) {
        JComponent component = configuration.getSettingsEditor().getComponent();
        if (component != null) {
            TabInfo tabInfo = getTabInfo(component);
            configurationTabs.select(tabInfo, true);
        }
    }

    private TabInfo getTabInfo(JComponent component) {
        for (TabInfo tabInfo : configurationTabs.getTabs()) {
            JBScrollPane scrollPane = (JBScrollPane) tabInfo.getComponent();
            if (scrollPane.getViewport().getView() == component) {
                return tabInfo;
            }
        }
        return null;
    }

    @NotNull
    public Configuration getActiveSettings() {
        TabInfo tabInfo = configurationTabs.getSelectedInfo();
        if (tabInfo != null) {
            return (Configuration) tabInfo.getObject();
        }
        return getConfiguration();
    }

    public void dispose() {
        configurationTabs.dispose();
        super.dispose();
    }
}
