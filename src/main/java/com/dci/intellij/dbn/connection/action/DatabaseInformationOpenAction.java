package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseInformationOpenAction extends AbstractConnectionAction {

    DatabaseInformationOpenAction(ConnectionHandler connection) {
        super("Connection Info", connection);
        //getTemplatePresentation().setEnabled(connection.getConnectionStatus().isConnected());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        ConnectionAction.invoke("showing database information", true, connection,
                action -> Progress.prompt(project, connection, false,
                        "Loading database information",
                        "Loading database information for " + connection.getQualifiedName(),
                        progress -> ConnectionManager.showConnectionInfoDialog(connection)));
    }
}
