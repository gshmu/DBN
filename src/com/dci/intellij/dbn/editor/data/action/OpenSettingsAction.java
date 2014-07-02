package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenSettingsAction extends DumbAwareAction {
    public OpenSettingsAction() {
        super("Settings", null, Icons.ACTION_SETTINGS);
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
            DataEditorSettings settings = DataEditorSettings.getInstance(project);
            globalSettingsDialog.focusSettings(settings);
            globalSettingsDialog.show();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Settings");
    }
}
