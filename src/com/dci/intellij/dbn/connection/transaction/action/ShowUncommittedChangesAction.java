package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.action.AbstractConnectionAction;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ShowUncommittedChangesAction extends AbstractConnectionAction {

    public ShowUncommittedChangesAction(ConnectionHandler connectionHandler) {
        super("Show uncommitted changes", connectionHandler);

    }

    public void actionPerformed(AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Project project = connectionHandler.getProject();
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.showUncommittedChangesDialog(connectionHandler, null);
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(getConnectionHandler().hasUncommittedChanges());
    }
}
