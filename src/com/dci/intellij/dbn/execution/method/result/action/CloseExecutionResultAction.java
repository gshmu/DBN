package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class CloseExecutionResultAction extends MethodExecutionResultAction {
    public CloseExecutionResultAction(MethodExecutionResult executionResult) {
        super(executionResult, "Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = executionResult.getProject();
        ExecutionManager.getInstance(project).removeResultTab(executionResult);
    }
}
