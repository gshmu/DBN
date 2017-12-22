package com.dci.intellij.dbn.data.grid.ui.table.resultSet.record;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;

public class ResultSetRecordViewerDialog extends DBNDialog<ResultSetRecordViewerForm> {
    private ResultSetTable table;
    private boolean showDataTypes;
    public ResultSetRecordViewerDialog(ResultSetTable table, boolean showDataTypes) {
        super(table.getProject(), "View record", true);
        this.table = table;
        this.showDataTypes = showDataTypes;
        setModal(true);
        setResizable(true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    @NotNull
    @Override
    protected ResultSetRecordViewerForm createComponent() {
        return new ResultSetRecordViewerForm(this, table, showDataTypes);
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
        table = null;
    }
}
