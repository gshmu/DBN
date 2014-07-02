package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementViewerPopup;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.Component;

public class ExecutionResultViewStatementAction extends AbstractExecutionResultAction {
    public ExecutionResultViewStatementAction() {
        super("View SQL statement", Icons.FILE_BLOCK_SQL);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementViewerPopup statementViewer = new StatementViewerPopup(executionResult);
            statementViewer.show((Component) e.getInputEvent().getSource());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("View SQL statement");
    }
}
