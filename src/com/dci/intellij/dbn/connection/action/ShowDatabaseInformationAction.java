package com.dci.intellij.dbn.connection.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ShowDatabaseInformationAction extends AbstractConnectionAction {

    public ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Info", connectionHandler);
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        TaskInstructions taskInstructions = new TaskInstructions("Loading database information for " + connectionHandler.getName(), false, false);
        new ConnectionAction("showing database information", connectionHandler, taskInstructions) {
            @Override
            protected void execute() {
                Project project = getProject();
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionHandler connectionHandler = getConnectionHandler();
                connectionManager.showConnectionInfoDialog(connectionHandler);
            }
        }.start();
    }
}
