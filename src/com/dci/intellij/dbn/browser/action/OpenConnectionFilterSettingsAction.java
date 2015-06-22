package com.dci.intellij.dbn.browser.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ui.ConnectionFilterSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenConnectionFilterSettingsAction extends DumbAwareAction {
    public OpenConnectionFilterSettingsAction() {
        super("Object Filter Settings...", null, Icons.DATASET_FILTER);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            ConnectionHandler activeConnection = browserManager.getActiveConnection();
            if (activeConnection != null) {
                ConnectionFilterSettingsDialog filterSettingsDialog = new ConnectionFilterSettingsDialog(activeConnection);
                filterSettingsDialog.show();
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Object Filter Settings...");
        Project project = ActionUtil.getProject(e);
        ConnectionHandler activeConnection = null;
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            activeConnection = browserManager.getActiveConnection();
        }
        presentation.setEnabled(activeConnection != null);
    }
}
