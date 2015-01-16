package com.dci.intellij.dbn.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;

public class ProjectSettingsEditorForm extends CompositeConfigurationEditorForm<ProjectSettings> {
    private JPanel mainPanel;
    private TabbedPane configurationTabs;

    public ProjectSettingsEditorForm(ProjectSettings globalSettings) {
        super(globalSettings);

        configurationTabs = new TabbedPane(this);
        //configurationTabs.setAdjustBorders(false);

        mainPanel.add(configurationTabs, BorderLayout.CENTER);

        ConnectionBundleSettings connectionSettings = globalSettings.getConnectionSettings();
        DatabaseBrowserSettings browserSettings = globalSettings.getBrowserSettings();
        NavigationSettings navigationSettings = globalSettings.getNavigationSettings();
        CodeEditorSettings codeEditorSettings = globalSettings.getCodeEditorSettings();
        CodeCompletionSettings codeCompletionSettings = globalSettings.getCodeCompletionSettings();
        DataGridSettings dataGridSettings = globalSettings.getDataGridSettings();
        DataEditorSettings dataEditorSettings = globalSettings.getDataEditorSettings();
        ExecutionEngineSettings executionEngineSettings = globalSettings.getExecutionEngineSettings();
        DDLFileSettings ddlFileSettings = globalSettings.getDdlFileSettings();
        GeneralProjectSettings generalSettings = globalSettings.getGeneralSettings();

        addSettingsPanel(connectionSettings);
        addSettingsPanel(browserSettings);
        addSettingsPanel(navigationSettings);
        addSettingsPanel(codeEditorSettings);
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

    public void focusConnectionSettings(@Nullable ConnectionHandler connectionHandler) {
        ConnectionBundleSettings connectionSettings = getConfiguration().getConnectionSettings();
        ConnectionBundleSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.selectConnection(connectionHandler);
            focusSettingsEditor(ConfigId.CONNECTIONS);
        }
    }

    public void focusSettingsEditor(ConfigId configId) {
        Configuration configuration = getConfiguration().getConfiguration(configId);
        if (configuration != null) {
            ConfigurationEditorForm settingsEditor = configuration.getSettingsEditor();
            if (settingsEditor != null) {
                JComponent component = settingsEditor.getComponent();
                if (component != null) {
                    TabInfo tabInfo = getTabInfo(component);
                    configurationTabs.select(tabInfo, true);
                }
            }
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
