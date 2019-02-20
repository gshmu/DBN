package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class TransactionRollbackAction extends AbstractConnectionAction {

    public TransactionRollbackAction(ConnectionHandler connectionHandler) {
        super("Rollback", "Rollback connection", Icons.CONNECTION_ROLLBACK, connectionHandler);

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseTransactionManager transactionManager = ActionUtil.getComponent(e, DatabaseTransactionManager.class);
        transactionManager.rollback(connectionHandler, null, false, false, null);
    }

    @Override
    public void update(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connectionHandler.hasUncommittedChanges());
        presentation.setVisible(!connectionHandler.isAutoCommit());
    }
}
