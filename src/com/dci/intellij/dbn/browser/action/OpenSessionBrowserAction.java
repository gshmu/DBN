package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenSessionBrowserAction extends DumbAwareProjectAction {
    public OpenSessionBrowserAction() {
        super("Open Session Browser", "", Icons.FILE_SESSION_BROWSER);
    }

    private static ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
        Project project = Lookup.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getActiveConnection();
        }
        return null;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null) {
            presentation.setEnabled(true);
            presentation.setVisible(DatabaseFeature.SESSION_BROWSING.isSupported(connectionHandler));
        } else {
            presentation.setVisible(false);
            presentation.setEnabled(false);
        }
        presentation.setText("Open Session Browser");
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null) {
            SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
            sessionBrowserManager.openSessionBrowser(connectionHandler);
        }

    }

}
