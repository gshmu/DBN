package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class PromptMethodExecutionAction extends MethodExecutionResultAction {
    public PromptMethodExecutionAction(MethodExecutionResult executionResult) {
        super(executionResult, "Open execution dialog", Icons.METHOD_EXECUTION_DIALOG);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = executionResult.getProject();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        MethodExecutionInput executionInput = executionResult.getExecutionInput();
        if (executionManager.promptExecutionDialog(executionInput, false)) {
            executionManager.execute(executionInput);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(
                !executionResult.isDebug() &&
                !executionResult.getExecutionInput().isExecuting());
    }    
}