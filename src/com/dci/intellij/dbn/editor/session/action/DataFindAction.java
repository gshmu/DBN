package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DataFindAction extends AbstractSessionBrowserAction {
    public DataFindAction() {
        super("Find...", Icons.ACTION_FIND);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.showSearchHeader();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        e.getPresentation().setEnabled(sessionBrowser != null);
        e.getPresentation().setText("Find...");
    }
}