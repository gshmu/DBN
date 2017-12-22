package com.dci.intellij.dbn.execution.statement.result.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.dialog.DialogWithTimeout;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import static com.dci.intellij.dbn.execution.ExecutionStatus.PROMPTED;

public class PendingTransactionDialog extends DialogWithTimeout {
    private CommitAction commitAction;
    private RollbackAction rollbackAction;
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
        DisposerUtil.register(this, transactionForm);
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
        ConnectionUtil.rollback(connection);
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
            super("Commit", Icons.CONNECTION_COMMIT);
            putValue(DEFAULT_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                DBNConnection connection = getConnection();
                ConnectionUtil.commit(connection);
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

        public void actionPerformed(ActionEvent e) {
            try {
                DBNConnection connection = getConnection();
                ConnectionUtil.rollback(connection);
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
        ConnectionUtil.rollback(connection);
        super.doCancelAction();
    }

    @Override
    public void dispose() {
        super.dispose();
        executionProcessor = null;
    }


}
