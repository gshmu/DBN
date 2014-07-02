package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class ToggleAutoCommitAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public ToggleAutoCommitAction(ConnectionHandler connectionHandler) {
        super("Turn Auto-Commit ON/OFF");
        this.connectionHandler = connectionHandler;

    }

    public void actionPerformed(AnActionEvent e) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
        transactionManager.toggleAutoCommit(connectionHandler);
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setText(
                connectionHandler.isAutoCommit() ?
                        "Turn Auto-Commit OFF" :
                        "Turn Auto-Commit ON");
    }
}
