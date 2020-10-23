package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RenameExecutionResultDialog extends DBNDialog<RenameExecutionResultForm> {
    private StatementExecutionResult executionResult;

    public RenameExecutionResultDialog(StatementExecutionResult executionResult) {
        super(executionResult.getProject(), "Rename result", true);
        this.executionResult = executionResult;
        getOKAction().putValue(Action.NAME, "Rename");
        init();
    }

    @NotNull
    @Override
    protected RenameExecutionResultForm createForm() {
        return new RenameExecutionResultForm(this, executionResult);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getOKAction(),
            getCancelAction()
        };
    }

    @Override
    protected void doOKAction() {
        DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(getProject());
        RenameExecutionResultForm component = getForm();
        executionResult.setName(component.getResultName());
        super.doOKAction();
    }

    @Override
    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }
}
