package com.dci.intellij.dbn.execution.statement.result.action;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public abstract class AbstractExecutionResultAction extends DumbAwareAction {
    protected AbstractExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public StatementExecutionCursorResult getExecutionResult(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            return e.getData(DBNDataKeys.STATEMENT_EXECUTION_CURSOR_RESULT);
        }
        return null;
    }

    @Override
    public void update(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        e.getPresentation().setEnabled(executionResult != null);
    }

}
