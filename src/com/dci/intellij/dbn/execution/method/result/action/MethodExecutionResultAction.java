package com.dci.intellij.dbn.execution.method.result.action;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public abstract class MethodExecutionResultAction extends DumbAwareAction {
    protected MethodExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public MethodExecutionResult getExecutionResult(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            return e.getData(DBNDataKeys.METHOD_EXECUTION_RESULT);
        }
        return null;
    }

    @Override
    public void update(AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        e.getPresentation().setEnabled(executionResult != null);
    }

}
