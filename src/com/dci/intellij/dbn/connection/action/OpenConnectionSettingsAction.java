package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenConnectionSettingsAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public OpenConnectionSettingsAction(ConnectionHandler connectionHandler) {
        super("Settings", "Connection settings", Icons.ACTION_EDIT);
        this.connectionHandler = connectionHandler;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
            globalSettingsDialog.focusConnectionSettings(connectionHandler);
            globalSettingsDialog.show();
        }
    }
}
