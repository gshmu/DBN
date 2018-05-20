package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class CopyConnectionsAction extends ConnectionSettingsAction {
    public CopyConnectionsAction() {
        super("Copy to Clipboard", Icons.CONNECTION_COPY);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        if (settingsForm != null) {
            settingsForm.copyConnectionsToClipboard();
        }
    }

    public void update(@NotNull AnActionEvent e) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        e.getPresentation().setEnabled(settingsForm != null && settingsForm.getSelectionSize() > 0);
        e.getPresentation().setText("Copy to Clipboard");
    }
}
