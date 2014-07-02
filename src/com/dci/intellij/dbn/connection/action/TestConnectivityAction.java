package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TestConnectivityAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public TestConnectivityAction(ConnectionHandler connectionHandler) {
        super("Test connectivity", "Test connectivity of " + connectionHandler.getName(), null);
        this.connectionHandler = connectionHandler;
        getTemplatePresentation().setEnabled(!connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = connectionHandler.getProject();
        new BackgroundTask(project, "Trying to connect to " + connectionHandler.getName(), false) {
            @Override
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true);
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                connectionManager.testConnection(connectionHandler, true);
            }
        }.start();

    }
}
