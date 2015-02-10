package com.dci.intellij.dbn.editor.session.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class KillSessionsImmediateAction extends AbstractSessionBrowserAction {

    public KillSessionsImmediateAction() {
        super("Kill Sessions", Icons.ACTION_KILL_SESSION_IMMEDIATE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.killSelectedSessions(true);
        }
    }

    public void update(AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        boolean visible = false;
        String text = "Kill Sessions";
        if (sessionBrowser != null) {
            ConnectionHandler connectionHandler = sessionBrowser.getConnectionHandler();
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            boolean canKillImmediate = compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_KILL_IMMEDIATE);
            boolean canKillPostTransaction = compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_KILL_POST_TRANSACTION);
            visible = canKillImmediate;
            if (canKillPostTransaction) text = "Kill Sessions Immediate";
        }

        Presentation presentation = e.getPresentation();
        presentation.setText(text);
        presentation.setVisible(visible);
    }
}