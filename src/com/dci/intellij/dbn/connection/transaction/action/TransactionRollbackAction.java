package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TransactionRollbackAction extends AbstractConnectionAction {

    public TransactionRollbackAction(ConnectionHandler connectionHandler) {
        super("Rollback", "Rollback connection", Icons.CONNECTION_ROLLBACK, connectionHandler);

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.rollback(connectionHandler, null, false, false, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, Project project, @NotNull ConnectionHandler connectionHandler) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connectionHandler.hasUncommittedChanges());
        presentation.setVisible(!connectionHandler.isAutoCommit());
    }
}
