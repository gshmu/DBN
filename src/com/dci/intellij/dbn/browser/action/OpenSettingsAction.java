package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class OpenSettingsAction extends AnAction {
    public OpenSettingsAction() {
        super("Settings", null, Icons.ACTION_SETTINGS);
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
            DatabaseBrowserSettings databaseBrowserSettings = DatabaseBrowserSettings.getInstance(project);
            globalSettingsDialog.focusSettings(databaseBrowserSettings);
            globalSettingsDialog.show();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Settings");
    }
}
