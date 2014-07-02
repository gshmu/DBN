package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenSettingsAction extends DumbAwareAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null && !project.isDisposed()) {
            GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
            globalSettingsDialog.show();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        presentation.setEnabled(project != null);
        if (e.getPlace().equals(ActionPlaces.MAIN_MENU)) {
            presentation.setIcon(null);
            presentation.setText("Settings...");
        }
    }

}
