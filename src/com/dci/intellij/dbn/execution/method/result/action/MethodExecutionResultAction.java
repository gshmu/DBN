package com.dci.intellij.dbn.execution.method.result.action;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public abstract class MethodExecutionResultAction extends DumbAwareAction {
    protected MethodExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public MethodExecutionResult getExecutionResult(AnActionEvent e) {
        MethodExecutionResult result = e.getData(DBNDataKeys.METHOD_EXECUTION_RESULT);
        if (result == null) {
            Project project = FailsafeUtil.get(e.getProject());
            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
            if (executionResult instanceof MethodExecutionResult) {
                return (MethodExecutionResult) executionResult;
            }
        }

        return result;
    }

    @Override
    public void update(AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        e.getPresentation().setEnabled(executionResult != null);
    }

}
