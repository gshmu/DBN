package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;

public class StartMethodExecutionAction extends MethodExecutionResultAction {
    public StartMethodExecutionAction() {
        super("Execute Again", Icons.METHOD_EXECUTION_RERUN);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull MethodExecutionResult executionResult) {
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.execute(executionResult.getExecutionInput());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable MethodExecutionResult executionResult) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Execute Again");
        presentation.setEnabled(
                executionResult != null &&
                !executionResult.getDebuggerType().isDebug() &&
                executionResult.getExecutionContext().isNot(EXECUTING));
    }
}