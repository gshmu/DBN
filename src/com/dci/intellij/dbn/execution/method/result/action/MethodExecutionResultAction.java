package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class MethodExecutionResultAction extends DumbAwareProjectAction {
    MethodExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    private MethodExecutionResult getExecutionResult(AnActionEvent e) {
        MethodExecutionResult result = e.getData(DataKeys.METHOD_EXECUTION_RESULT);
        if (result == null ) {
            Project project = e.getProject();
            if (project != null) {
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
                if (executionResult instanceof MethodExecutionResult) {
                    return (MethodExecutionResult) executionResult;
                }
            }
        }

        return result;
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        MethodExecutionResult executionResult = getExecutionResult(e);
        if (Failsafe.check(executionResult)) {
            actionPerformed(e, project, executionResult);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        MethodExecutionResult executionResult = getExecutionResult(e);

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(executionResult != null);
        update(e, project, executionResult);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MethodExecutionResult executionResult);


    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable MethodExecutionResult executionResult);

}
