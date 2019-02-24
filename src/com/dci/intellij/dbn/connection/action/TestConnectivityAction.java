package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class TestConnectivityAction extends AbstractConnectionAction {

    public TestConnectivityAction(ConnectionHandler connectionHandler) {
        super("Test connectivity", "Test connectivity of " + connectionHandler.getName(), null, connectionHandler);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final ConnectionHandler connectionHandler = getConnectionHandler();
        connectionHandler.getInstructions().setAllowAutoConnect(true);
        ConnectionAction.invoke(
                "testing the connectivity",
                instructions("Trying to connect to " + connectionHandler.getName(), TaskInstruction.MANAGED),
                connectionHandler,
                action -> ConnectionManager.testConnection(connectionHandler, null, SessionId.TEST, true, true));
    }
}
