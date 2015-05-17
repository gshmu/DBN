package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class TransactionCommitAction extends AbstractConnectionAction {

    public TransactionCommitAction(ConnectionHandler connectionHandler) {
        super("Commit", "Commit connection", Icons.CONNECTION_COMMIT, connectionHandler);

    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
            transactionManager.commit(getConnectionHandler(), false, false);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connectionHandler.hasUncommittedChanges());
        presentation.setVisible(!connectionHandler.isAutoCommit());
    }
}
