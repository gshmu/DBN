package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExecutionResultViewRecordAction extends AbstractExecutionResultAction {
    public ExecutionResultViewRecordAction() {
        super("View record", Icons.EXEC_RESULT_VIEW_RECORD);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (resultTable != null) {
                resultTable.showRecordViewDialog();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        boolean enabled = executionResult != null && executionResult.getResultTable() != null && executionResult.getResultTable().getSelectedColumn() > -1;
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setText("View Record");
    }
}
