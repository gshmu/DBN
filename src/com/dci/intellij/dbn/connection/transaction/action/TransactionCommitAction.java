package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class TransactionCommitAction extends AbstractConnectionAction {

    public TransactionCommitAction(ConnectionHandler connectionHandler) {
        super("Commit", "Commit connection", Icons.CONNECTION_COMMIT, connectionHandler);

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseTransactionManager transactionManager = ActionUtil.getComponent(e, DatabaseTransactionManager.class);
        transactionManager.commit(connectionHandler, null, false, false, null);
    }

    @Override
    public void update(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connectionHandler.hasUncommittedChanges());
        presentation.setVisible(!connectionHandler.isAutoCommit());
    }
}
