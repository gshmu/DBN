package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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
        new BackgroundTask(project, "Trying to connect to " + connectionHandler.getName(), false) {
            @Override
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true);
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                connectionManager.testConnection(connectionHandler, false, true);
            }
        }.start();
    }
}
