package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class MethodExecutionCursorResultAction extends DumbAwareProjectAction {
    MethodExecutionCursorResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    ResultSetTable getResultSetTable(AnActionEvent e) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        return cursorResultForm == null ? null : cursorResultForm.getTable();
    }

    @Nullable
    MethodExecutionCursorResultForm getCursorResultForm(AnActionEvent e) {
        return e.getData(DataKeys.METHOD_EXECUTION_CURSOR_RESULT_FORM);
    }

    @Nullable
    DBArgument getMethodArgument(AnActionEvent e) {
        return e.getData(DataKeys.METHOD_EXECUTION_ARGUMENT);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(resultSetTable != null);
    }
}
