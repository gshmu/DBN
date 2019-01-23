package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionToggleAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class AutoCommitToggleAction extends AbstractConnectionToggleAction {

    public AutoCommitToggleAction(ConnectionHandler connectionHandler) {
        super("Auto-Commit", connectionHandler);

    }
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return getConnectionHandler().isAutoCommit();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
        transactionManager.toggleAutoCommit(connectionHandler);
    }
}
