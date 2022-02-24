package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserOpenAction extends DumbAwareProjectAction {
    public SessionBrowserOpenAction() {
        super("Open Session Browser...", null, Icons.FILE_SESSION_BROWSER);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();

        ConnectionHandler singleConnectionHandler = null;
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionBundle.getConnections().size() > 0) {
            actionGroup.addSeparator();
            for (ConnectionHandler connection : connectionBundle.getConnections()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connection);
                actionGroup.add(connectionAction);
                singleConnectionHandler = connection;
            }
        }

        if (actionGroup.getChildrenCount() > 1) {
            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Select Session Browser Connection",
                    actionGroup,
                    e.getDataContext(),
                    //JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    false,
                    true,
                    true,
                    null,
                    actionGroup.getChildrenCount(), null);

            popupBuilder.showCenteredInCurrentWindow(project);
        } else {
            if (singleConnectionHandler != null) {
                openSessionBrowser(singleConnectionHandler);
            } else {
                Messages.showInfoDialog(project, "No connections available.", "No database connections found. Please setup a connection first");
            }

        }

    }

    private class SelectConnectionAction extends AbstractConnectionAction{

        SelectConnectionAction(ConnectionHandler connection) {
            super(connection.getName(), connection.getIcon(), connection);
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
            openSessionBrowser(connection);
        }
    }

    private static void openSessionBrowser(ConnectionHandler connection) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(connection.getProject());
        sessionBrowserManager.openSessionBrowser(connection);
    }
}
