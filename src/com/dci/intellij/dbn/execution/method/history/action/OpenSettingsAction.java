package com.dci.intellij.dbn.execution.method.history.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenSettingsAction extends DumbAwareAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ProjectSettingsDialog globalSettingsDialog = new ProjectSettingsDialog(project);
            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(project);
            globalSettingsDialog.focusSettings(settings);
            globalSettingsDialog.show();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_SETTINGS);
        presentation.setText("Settings");
    }
}
