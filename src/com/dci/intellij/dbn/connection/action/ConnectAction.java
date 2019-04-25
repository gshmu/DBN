package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AbstractConnectionAction {
    ConnectAction(ConnectionHandler connectionHandler) {
        super("Connect", "Connect to " + connectionHandler.getName(), null, connectionHandler);
        getTemplatePresentation().setEnabled(!connectionHandler.getConnectionStatus().isConnected());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        connectionHandler.getInstructions().setAllowAutoConnect(true);

        ConnectionAction.invoke("", true, connectionHandler,
                (action) -> ConnectionManager.testConnection(connectionHandler, null, SessionId.MAIN, false, true));
    }

}
