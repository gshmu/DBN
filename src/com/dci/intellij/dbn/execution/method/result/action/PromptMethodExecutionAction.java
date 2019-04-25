package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;

public class PromptMethodExecutionAction extends MethodExecutionResultAction {
    public PromptMethodExecutionAction() {
        super("Open Execution Dialog", Icons.METHOD_EXECUTION_DIALOG);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull MethodExecutionResult executionResult) {
        MethodExecutionInput executionInput = executionResult.getExecutionInput();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.startMethodExecution(executionInput, DBDebuggerType.NONE);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable MethodExecutionResult executionResult) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Open Execution Dialog");
        presentation.setEnabled(
                executionResult != null &&
                !executionResult.getDebuggerType().isDebug() &&
                executionResult.getExecutionContext().isNot(EXECUTING));
    }    
}