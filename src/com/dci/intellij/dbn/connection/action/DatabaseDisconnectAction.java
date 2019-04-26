package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseDisconnectAction extends AbstractConnectionAction {
    DatabaseDisconnectAction(ConnectionHandler connectionHandler) {
        super("Disconnect", "Disconnect from " + connectionHandler.getName(), null, connectionHandler);
        getTemplatePresentation().setEnabled(connectionHandler.getConnectionStatus().isConnected());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
        transactionManager.disconnect(connectionHandler);
    }
}
