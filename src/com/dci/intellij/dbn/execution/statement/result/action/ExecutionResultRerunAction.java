package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class ExecutionResultRerunAction extends AbstractExecutionResultAction {
    public ExecutionResultRerunAction() {
        super("Rerun Statement", Icons.EXEC_RESULT_RERUN);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            executionResult.reload();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);

        boolean enabled = false;
        if (Failsafe.check(executionResult)) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (Failsafe.check(resultTable)) {
                enabled = !resultTable.isLoading();
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText("Rerun Statement");
    }
}
