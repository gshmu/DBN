package com.dci.intellij.dbn.execution.method.result.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class MethodExecutionCursorResultAction extends DumbAwareAction {
    protected MethodExecutionCursorResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    protected ResultSetTable getResultSetTable(AnActionEvent e) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        return cursorResultForm == null ? null : cursorResultForm.getTable();
    }

    @Nullable
    protected MethodExecutionCursorResultForm getCursorResultForm(AnActionEvent e) {
        return e.getData(DBNDataKeys.METHOD_EXECUTION_CURSOR_RESULT_FORM);
    }

    @Nullable
    public DBArgument getMethodArgument(AnActionEvent e) {
        return e.getData(DBNDataKeys.METHOD_EXECUTION_ARGUMENT);
    }

    @Override
    public void update(AnActionEvent e) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        e.getPresentation().setEnabled(resultSetTable != null);
    }

}
