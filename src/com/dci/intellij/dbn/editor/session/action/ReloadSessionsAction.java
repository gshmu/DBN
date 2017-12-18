package com.dci.intellij.dbn.editor.session.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ReloadSessionsAction extends AbstractSessionBrowserAction {

    public ReloadSessionsAction() {
        super("Reload", Icons.DATA_EDITOR_RELOAD_DATA);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.loadSessions(true);
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        presentation.setEnabled(sessionBrowser != null && !sessionBrowser.isLoading());
        presentation.setText("Reload");
        presentation.setEnabled(true);

    }
}