package com.dci.intellij.dbn.editor.session.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetLoadInstructions;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ReloadSessionsAction extends AbstractSessionBrowserAction {

    public static final DatasetLoadInstructions LOAD_INSTRUCTIONS = new DatasetLoadInstructions(true, true, true, false);

    public ReloadSessionsAction() {
        super("Reload", Icons.DATA_EDITOR_RELOAD_DATA);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.reload();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Reload");
        presentation.setEnabled(true);

    }
}