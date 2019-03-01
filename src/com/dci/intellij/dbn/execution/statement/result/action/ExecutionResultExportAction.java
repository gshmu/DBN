package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExecutionResultExportAction extends AbstractExecutionResultAction {
    public ExecutionResultExportAction() {
        super("Export data", Icons.DATA_EXPORT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (Failsafe.check(executionResult)) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (Failsafe.check(resultTable)) {
                ExportDataDialog dialog = new ExportDataDialog(resultTable, executionResult);
                dialog.show();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Export Data");
    }
}
