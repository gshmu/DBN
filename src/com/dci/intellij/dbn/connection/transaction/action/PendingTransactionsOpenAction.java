package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PendingTransactionsOpenAction extends AbstractConnectionAction {

    public PendingTransactionsOpenAction(ConnectionHandler connectionHandler) {
        super("Show uncommitted changes", connectionHandler);

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.showPendingTransactionsDialog(connectionHandler, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, Project project, @NotNull ConnectionHandler connectionHandler) {
        e.getPresentation().setEnabled(connectionHandler.hasUncommittedChanges());
    }
}
