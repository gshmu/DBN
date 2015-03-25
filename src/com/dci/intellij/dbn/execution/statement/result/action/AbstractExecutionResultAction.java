package com.dci.intellij.dbn.execution.statement.result.action;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public abstract class AbstractExecutionResultAction extends DumbAwareAction {
    protected AbstractExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public StatementExecutionCursorResult getExecutionResult(AnActionEvent e) {
        StatementExecutionCursorResult result = e.getData(DBNDataKeys.STATEMENT_EXECUTION_CURSOR_RESULT);
        if (result == null) {
            Project project = FailsafeUtil.get(e.getProject());
            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
            if (executionResult instanceof StatementExecutionCursorResult) {
                return (StatementExecutionCursorResult) executionResult;
            }
        }
        return result;
    }

    @Override
    public void update(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        e.getPresentation().setEnabled(executionResult != null);
    }

}
