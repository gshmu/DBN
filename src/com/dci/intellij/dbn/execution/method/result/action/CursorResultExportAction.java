package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CursorResultExportAction extends MethodExecutionCursorResultAction {
    public CursorResultExportAction() {
        super("Export Data", Icons.DATA_EXPORT);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        DBArgument methodArgument = getMethodArgument(e);
        if (resultSetTable != null && methodArgument != null) {
            ExportDataDialog dialog = new ExportDataDialog(resultSetTable, methodArgument);
            dialog.show();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Export Data");
    }
}
