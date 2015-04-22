package com.dci.intellij.dbn.connection.config.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class PasteConnectionAction extends ConnectionSettingsAction {
    public PasteConnectionAction() {
        super("Paste From Clipboard", Icons.CONNECTION_PASTE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionBundleSettingsForm settingsForm = getSettingsForm(e);
        if (settingsForm != null) {
            settingsForm.pasteConnectionsFromClipboard();
        }

    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
            if (clipboardData instanceof String) {
                String clipboardString = (String) clipboardData;
                presentation.setEnabled(clipboardString.contains("connection-configurations"));
            } else {
                presentation.setEnabled(false);
            }
        } catch (Exception ex) {
            presentation.setEnabled(false);
        }
        presentation.setText("Paste From Clipboard");

    }
}
