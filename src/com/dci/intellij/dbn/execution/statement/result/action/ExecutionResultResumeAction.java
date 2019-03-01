package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class ExecutionResultResumeAction extends AbstractExecutionResultAction {
    public ExecutionResultResumeAction() {
        super("Fetch next records", Icons.EXEC_RESULT_RESUME);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            executionResult.fetchNextRecords();
        }

    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        boolean enabled = false;

        if (Failsafe.check(executionResult) && executionResult.hasResult()) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (Failsafe.check(resultTable)) {
                ResultSetDataModel tableModel = resultTable.getModel();
                if (Failsafe.check(tableModel)) {
                    enabled = !resultTable.isLoading() && !tableModel.isResultSetExhausted();
                }
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText("Fetch Next Records");
    }
}
