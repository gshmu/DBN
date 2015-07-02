package com.dci.intellij.dbn.options.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class SettingsAction extends DumbAwareAction {
    private ConfigId configId;

    public SettingsAction(ConfigId configId) {
        super(configId.getName() + "...");
        this.configId = configId;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);

            if (configId == ConfigId.CONNECTIONS) {
                DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
                ConnectionHandler activeConnection = browserManager.getActiveConnection();
                String connectionId = activeConnection == null ? null : activeConnection.getId();
                settingsManager.openConnectionSettings(connectionId);
            }
             else {
                settingsManager.openProjectSettings(configId);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
/*
        presentation.setIcon(Icons.ACTION_SETTINGS);
        presentation.setText("Settings");
*/
    }
}
