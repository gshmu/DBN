package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class ConnectAction extends AbstractConnectionAction {
    public ConnectAction(ConnectionHandler connectionHandler) {
        super("Connect", "Connect to " + connectionHandler.getName(), null, connectionHandler);
        getTemplatePresentation().setEnabled(!connectionHandler.getConnectionStatus().isConnected());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getInstructions().setAllowAutoConnect(true);

        ConnectionAction.invoke(
                "connecting to database",
                instructions("Trying to connect to " + connectionHandler.getName(), TaskInstruction.MANAGED),
                connectionHandler,
                action -> ConnectionManager.testConnection(connectionHandler, null, SessionId.MAIN, false, true));
    }
}
