package com.dci.intellij.dbn.execution.statement.result.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;

public class StatementExecutionTransactionDialog extends DBNDialog<StatementExecutionTransactionForm> {
    private CommitAction commitAction;
    private RollbackAction rollbackAction;
    private StatementExecutionProcessor executionProcessor;

    public StatementExecutionTransactionDialog(StatementExecutionProcessor executionProcessor) {
        super(executionProcessor.getProject(), "Uncommitted changes", true);
        setModal(true);
        setResizable(true);
        this.executionProcessor = executionProcessor;
        commitAction = new CommitAction();
        rollbackAction = new RollbackAction();
        component = new StatementExecutionTransactionForm(this, executionProcessor);
        init();
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                commitAction,
                rollbackAction,
                getHelpAction()
        };
    }

    private class CommitAction extends AbstractAction {
        CommitAction() {
            super("Commit changes", Icons.CONNECTION_COMMIT);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            DBNConnection connection = getConnection();
            ConnectionUtil.commit(connection);
            doOKAction();
        }
    }

    DBNConnection getConnection() {
        return executionProcessor.getExecutionInput().getExecutionContext().getConnection();
    }

    private class RollbackAction extends AbstractAction {
        RollbackAction() {
            super("Rollback changes", Icons.CONNECTION_ROLLBACK);
        }

        public void actionPerformed(ActionEvent e) {
            DBNConnection connection = getConnection();
            ConnectionUtil.commit(connection);
            doOKAction();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        executionProcessor = null;
    }


}
