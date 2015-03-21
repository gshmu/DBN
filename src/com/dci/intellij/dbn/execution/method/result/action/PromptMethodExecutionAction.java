package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class PromptMethodExecutionAction extends MethodExecutionResultAction {
    public PromptMethodExecutionAction() {
        super("Open Execution Dialog", Icons.METHOD_EXECUTION_DIALOG);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            MethodExecutionResult executionResult = getExecutionResult(e);
            if (executionResult != null) {
                MethodExecutionInput executionInput = executionResult.getExecutionInput();
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                if (executionManager.promptExecutionDialog(executionInput, false)) {
                    executionManager.execute(executionInput);
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Open Execution Dialog");
        presentation.setEnabled(
                executionResult != null &&
                        !executionResult.isDebug() &&
                        !executionResult.getExecutionInput().isExecuting());
    }    
}