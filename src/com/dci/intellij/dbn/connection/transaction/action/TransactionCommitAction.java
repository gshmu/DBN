package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransactionCommitAction extends AbstractConnectionAction {

    public TransactionCommitAction(ConnectionHandler connectionHandler) {
        super("Commit", "Commit connection", Icons.CONNECTION_COMMIT, connectionHandler);

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connectionHandler) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.commit(connectionHandler, null, false, false, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler connectionHandler) {
        boolean enabled = connectionHandler != null && connectionHandler.hasUncommittedChanges();
        boolean visible = connectionHandler != null && !connectionHandler.isAutoCommit();

        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }
}
