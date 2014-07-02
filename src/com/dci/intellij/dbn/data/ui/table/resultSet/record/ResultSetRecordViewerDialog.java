package com.dci.intellij.dbn.data.ui.table.resultSet.record;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.ui.table.resultSet.ResultSetTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;

public class ResultSetRecordViewerDialog extends DBNDialog {
    private ResultSetRecordViewerForm viewerForm;

    public ResultSetRecordViewerDialog(ResultSetTable table) {
        super(table.getProject(), "View Record", true);
        setModal(true);
        setResizable(true);
        viewerForm = new ResultSetRecordViewerForm(table);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }


    protected String getDimensionServiceKey() {
        return "DBNavigator.DataRecordViewer";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return viewerForm.getPreferredFocusComponent();
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

    @Nullable
    protected JComponent createCenterPanel() {
        return viewerForm.getComponent();
    }

}
