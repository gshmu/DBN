package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class ConnectAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public ConnectAction(ConnectionHandler connectionHandler) {
        super("Connect", "Connect to " + connectionHandler.getName(), null);
        this.connectionHandler = connectionHandler;
        getTemplatePresentation().setEnabled(!connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = connectionHandler.getProject();
        connectionHandler.setAllowConnection(true);
        TaskInstructions taskInstructions = new TaskInstructions("Trying to connect to " + connectionHandler.getName(), false, false);
        new ConnectionAction("connecting to database", connectionHandler, taskInstructions) {
            @Override
            protected void execute() {
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                connectionManager.testConnection(connectionHandler, false, true);
            }
        }.start();
    }
}
