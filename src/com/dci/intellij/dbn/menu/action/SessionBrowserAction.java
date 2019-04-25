package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.util.MessageUtil;
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

public class SessionBrowserAction extends DumbAwareProjectAction {
    public SessionBrowserAction() {
        super("Open Session Browser...", null, Icons.FILE_SESSION_BROWSER);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        //FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();

        ConnectionHandler singleConnectionHandler = null;
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionBundle.getConnectionHandlers().size() > 0) {
            actionGroup.addSeparator();
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler);
                actionGroup.add(connectionAction);
                singleConnectionHandler = connectionHandler;
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
                MessageUtil.showInfoDialog(project, "No connections available.", "No database connections found. Please setup a connection first");
            }

        }

    }

    private class SelectConnectionAction extends AbstractConnectionAction{

        SelectConnectionAction(ConnectionHandler connectionHandler) {
            super(connectionHandler.getName(), connectionHandler.getIcon(), connectionHandler);
        }

        @Override
        protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
            openSessionBrowser(connectionHandler);
        }
    }

    private static void openSessionBrowser(ConnectionHandler connectionHandler) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(connectionHandler.getProject());
        sessionBrowserManager.openSessionBrowser(connectionHandler);
    }
}
