package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
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
        boolean enabled = false;
        if (Failsafe.check(executionResult)) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (Failsafe.check(resultTable)) {
                enabled = resultTable.getSelectedColumn() > -1;
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText("View Record");
    }
}
