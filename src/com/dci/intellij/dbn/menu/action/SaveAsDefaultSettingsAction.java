package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.DefaultProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class SaveAsDefaultSettingsAction extends DumbAwareAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null && !project.isDisposed()) {
            DefaultProjectSettingsManager.getInstance().saveDefaultProjectSettings(project);
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        presentation.setEnabled(project != null);
        presentation.setText("Save project settings as default");
    }

}
