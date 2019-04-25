package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowDatabaseInformationAction extends AbstractConnectionAction {

    ShowDatabaseInformationAction(ConnectionHandler connectionHandler) {
        super("Connection Info", connectionHandler);
        //getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        ConnectionAction.invoke("showing database information", true, connectionHandler,
                (action) -> Progress.prompt(project, "Loading database information for " + connectionHandler.getName(), false,
                        (progress) -> ConnectionManager.showConnectionInfoDialog(connectionHandler)));
    }
}
