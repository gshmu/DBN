package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowDatabaseInformationAction extends AbstractConnectionAction {

    public ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Info", connectionHandler);
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        TaskInstructions taskInstructions = new TaskInstructions("Loading database information for " + connectionHandler.getName());
        new ConnectionAction("showing database information", connectionHandler, taskInstructions) {
            @Override
            protected void execute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                ConnectionManager.showConnectionInfoDialog(connectionHandler);
            }

            @Override
            protected boolean isManaged() {
                return true;
            }
        }.start();
    }
}
