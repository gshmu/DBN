package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowDatabaseInformationAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Information", null, null);
        this.connectionHandler = connectionHandler;
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        final Project project = connectionHandler.getProject();
        new BackgroundTask(project, "Loading database information for " + connectionHandler.getName(), false) {
            @Override
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true);

                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                connectionManager.showConnectionInfo(connectionHandler.getSettings());
            }
        }.start();

    }
}
