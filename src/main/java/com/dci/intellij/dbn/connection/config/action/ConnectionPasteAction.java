package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;

public class ConnectionPasteAction extends ConnectionSettingsAction {
    public ConnectionPasteAction() {
        super("Paste From Clipboard", Icons.CONNECTION_PASTE);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {

            target.pasteConnectionsFromClipboard();
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable ConnectionBundleSettingsForm target) {

        try {
            CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
            Object data = copyPasteManager.getContents(DataFlavor.stringFlavor);;
            if (data instanceof String) {
                String clipboardString = (String) data;
                presentation.setEnabled(clipboardString.contains("connection-configurations"));
            } else {
                presentation.setEnabled(false);
            }
        } catch (Exception ex) {
            presentation.setEnabled(false);
        }
    }
}
