package com.dci.intellij.dbn.options.ui;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ProjectSettingsEditorForm extends CompositeConfigurationEditorForm<ProjectSettings> {
    private JPanel mainPanel;
    private JPanel tabsPanel;
    private final TabbedPane configurationTabs;

    private WeakRef<ProjectSettingsDialog> dialogRef;

    public ProjectSettingsEditorForm(ProjectSettings globalSettings) {
        super(globalSettings);

        configurationTabs = new TabbedPane(this);
        //configurationTabs.setAdjustBorders(false);

        tabsPanel.add(configurationTabs, BorderLayout.CENTER);

        ConnectionBundleSettings connectionSettings = globalSettings.getConnectionSettings();
        DatabaseBrowserSettings browserSettings = globalSettings.getBrowserSettings();
        NavigationSettings navigationSettings = globalSettings.getNavigationSettings();
        CodeEditorSettings codeEditorSettings = globalSettings.getCodeEditorSettings();
        CodeCompletionSettings codeCompletionSettings = globalSettings.getCodeCompletionSettings();
        DataGridSettings dataGridSettings = globalSettings.getDataGridSettings();
        DataEditorSettings dataEditorSettings = globalSettings.getDataEditorSettings();
        ExecutionEngineSettings executionEngineSettings = globalSettings.getExecutionEngineSettings();
        OperationSettings operationSettings = globalSettings.getOperationSettings();
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
        addSettingsPanel(operationSettings);
        addSettingsPanel(ddlFileSettings);
        addSettingsPanel(generalSettings);
        globalSettings.reset();

        tabsPanel.setFocusable(true);
   }

    public void setDialog(ProjectSettingsDialog dialog) {
        this.dialogRef = WeakRef.of(dialog);
    }

    private void addSettingsPanel(BasicConfiguration configuration) {
        JComponent component = configuration.createComponent();
        JBScrollPane scrollPane = new JBScrollPane(component);
        TabInfo tabInfo = new TabInfo(scrollPane);
        tabInfo.setText(configuration.getDisplayName());
        tabInfo.setObject(configuration.getSettingsEditor());
        //tabInfo.setTabColor(GUIUtil.getWindowColor());
        configurationTabs.addTab(tabInfo);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    void selectConnectionSettings(@Nullable ConnectionId connectionId) {
        ConnectionBundleSettings connectionSettings = getConfiguration().getConnectionSettings();
        ConnectionBundleSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.selectConnection(connectionId);
            selectSettingsEditor(ConfigId.CONNECTIONS);
        }
    }

    void selectSettingsEditor(ConfigId configId) {
        Configuration configuration = getConfiguration().getConfiguration(configId);
        if (configuration != null) {
            ConfigurationEditorForm settingsEditor = configuration.getSettingsEditor();
            if (settingsEditor != null) {
                JComponent component = settingsEditor.getComponent();
                TabInfo tabInfo = getTabInfo(component);
                if (tabInfo != null) {
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
            ConfigurationEditorForm configurationEditorForm = (ConfigurationEditorForm) tabInfo.getObject();
            return configurationEditorForm.getConfiguration();
        }
        return getConfiguration();
    }
}
