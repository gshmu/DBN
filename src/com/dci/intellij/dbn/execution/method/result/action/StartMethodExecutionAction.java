package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
           
public class StartMethodExecutionAction extends MethodExecutionResultAction {
    public StartMethodExecutionAction(MethodExecutionResult executionResult) {
        super(executionResult, "Execute again", Icons.METHOD_EXECUTION_RERUN);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(executionResult.getProject());
        executionManager.execute(executionResult.getExecutionInput());
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(
                !executionResult.isDebug() &&
                !executionResult.getExecutionInput().isExecuting());
    }
}