package com.dci.intellij.dbn.browser.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenBrowserSettingsAction extends DumbAwareAction {
    public OpenBrowserSettingsAction() {
        super("Browser Settings...");
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
            settingsManager.openProjectSettings(ConfigId.BROWSER);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Browser Settings...");
    }
}
