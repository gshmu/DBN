package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class ShowDatabaseInformationAction extends AbstractConnectionAction {

    public ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Info", connectionHandler);
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ConnectionAction.invoke(
                instructions("Loading database information for " + connectionHandler.getName(), TaskInstruction.MANAGED),
                "showing database information",
                connectionHandler,
                action -> {
                    Failsafe.ensure(connectionHandler);
                    ConnectionManager.showConnectionInfoDialog(connectionHandler);
                });
    }
}
