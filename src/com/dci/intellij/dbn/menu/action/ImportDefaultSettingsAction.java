package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ImportDefaultSettingsAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
        projectSettingsManager.importDefaultSettings(false);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        presentation.setEnabled(project != null);
        presentation.setText("Import Settings...");
    }

}
