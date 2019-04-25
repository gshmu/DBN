package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionResultViewRecordAction extends AbstractExecutionResultAction {
    public ExecutionResultViewRecordAction() {
        super("View Record", Icons.EXEC_RESULT_VIEW_RECORD);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        ResultSetTable resultTable = executionResult.getResultTable();
        if (resultTable != null) {
            resultTable.showRecordViewDialog();
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable StatementExecutionCursorResult executionResult) {
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
