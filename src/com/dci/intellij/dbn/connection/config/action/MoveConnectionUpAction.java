package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MoveConnectionUpAction extends ConnectionSettingsAction {
    public MoveConnectionUpAction() {
        super("Move Connections Up", Icons.ACTION_MOVE_UP);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(settingsForm != null && settingsForm.getSelectionSize() > 0);
        presentation.setText("Move Connections Up");
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        if (settingsForm != null) {
            settingsForm.moveSelectedConnectionsUp();
        }
    }
}
