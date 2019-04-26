package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

public class PasteConnectionAction extends ConnectionSettingsAction {
    public PasteConnectionAction() {
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
    }
}
