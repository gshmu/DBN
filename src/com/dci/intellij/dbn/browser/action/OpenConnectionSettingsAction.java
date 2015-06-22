package com.dci.intellij.dbn.browser.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenConnectionSettingsAction extends DumbAwareAction {
    public OpenConnectionSettingsAction() {
        super("Connection Settings...");
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            ConnectionHandler activeConnection = browserManager.getActiveConnection();
            String connectionId = activeConnection == null ? null : activeConnection.getId();
            settingsManager.openConnectionSettings(connectionId);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Connection Settings...");
    }
}
