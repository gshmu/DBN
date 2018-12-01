package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ConnectAction extends AbstractConnectionAction {
    public ConnectAction(ConnectionHandler connectionHandler) {
        super("Connect", "Connect to " + connectionHandler.getName(), null, connectionHandler);
        getTemplatePresentation().setEnabled(!connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        final ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getInstructions().setAllowAutoConnect(true);
        TaskInstructions taskInstructions = new TaskInstructions(
                "Trying to connect to " + connectionHandler.getName(),
                TaskInstruction.MANAGED);

        ConnectionAction.invoke(
                "connecting to database",
                connectionHandler,
                taskInstructions,
                action -> ConnectionManager.testConnection(connectionHandler, false, true));
    }
}
