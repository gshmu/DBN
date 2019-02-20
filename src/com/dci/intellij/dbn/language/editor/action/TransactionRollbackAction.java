package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TransactionRollbackAction extends TransactionEditorAction {
    public TransactionRollbackAction() {
        super("Rollback", "Rollback changes", Icons.CONNECTION_ROLLBACK);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null) {
            DBNConnection connection = getConnection(e);
            if (connection != null) {
                Project project = ActionUtil.ensureProject(e);
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
                transactionManager.rollback(connectionHandler, connection, true, false, null);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Rollback");
    }

}
