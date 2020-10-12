package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DialogWithTimeout;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static com.dci.intellij.dbn.execution.ExecutionStatus.PROMPTED;

public class PendingTransactionDialog extends DialogWithTimeout {
    private final CommitAction commitAction;
    private final RollbackAction rollbackAction;
    private StatementExecutionProcessor executionProcessor;
    private PendingTransactionDialogForm transactionForm;

    public PendingTransactionDialog(StatementExecutionProcessor executionProcessor) {
        super(executionProcessor.getProject(), "Uncommitted changes", true, TimeUtil.getSeconds(5));
        setModal(true);
        setResizable(true);
        this.executionProcessor = executionProcessor;
        commitAction = new CommitAction();
        rollbackAction = new RollbackAction();
        transactionForm = new PendingTransactionDialogForm(this, executionProcessor);
        setModal(false);
        init();
    }

    @Override
    protected JComponent createContentComponent() {
        return transactionForm.getComponent();
    }

    @Override
    public void doDefaultAction() {
        DBNConnection connection = getConnection();
        ResourceUtil.rollbackSilently(connection);
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @Override
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
            super("Commit", Icons.CONNECTION_COMMIT);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                DBNConnection connection = getConnection();
                ResourceUtil.commitSilently(connection);
            } finally {
                executionProcessor.getExecutionContext().set(PROMPTED, false);
                executionProcessor.postExecute();
            }
            doOKAction();
        }
    }

    DBNConnection getConnection() {
        return executionProcessor.getExecutionInput().getExecutionContext().getConnection();
    }

    private class RollbackAction extends AbstractAction {
        RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                DBNConnection connection = getConnection();
                ResourceUtil.rollbackSilently(connection);
            } finally {
                executionProcessor.getExecutionContext().set(PROMPTED, false);
                executionProcessor.postExecute();
            }
            doOKAction();
        }
    }

    @Override
    public void doCancelAction() {
        DBNConnection connection = getConnection();
        ResourceUtil.rollbackSilently(connection);
        super.doCancelAction();
    }
}
