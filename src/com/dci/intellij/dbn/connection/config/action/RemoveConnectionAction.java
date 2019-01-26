package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class RemoveConnectionAction extends ConnectionSettingsAction {
    public RemoveConnectionAction() {
        super("Remove connection", Icons.ACTION_REMOVE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        if (settingsForm != null) {
            settingsForm.removeSelectedConnections();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        int length = settingsForm == null ? 0 : settingsForm.getSelectionSize();
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(length > 0);
        presentation.setText(length == 1 ? "Remove Connections" : "Remove Connection");
    }
}
