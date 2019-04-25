package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractExecutionResultAction extends DumbAwareProjectAction {
    protected AbstractExecutionResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    private static StatementExecutionCursorResult getExecutionResult(AnActionEvent e) {
        StatementExecutionCursorResult result = e.getData(DataKeys.STATEMENT_EXECUTION_CURSOR_RESULT);
        if (result == null) {
            Project project = e.getProject();
            if (project != null) {
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
                if (executionResult instanceof StatementExecutionCursorResult) {
                    return (StatementExecutionCursorResult) executionResult;
                }
            }
        }
        return result;
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (Failsafe.check(executionResult)) {
            actionPerformed(e, project, executionResult);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(Failsafe.check(executionResult));
        update(e, project, executionResult);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull StatementExecutionCursorResult executionResult);

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable StatementExecutionCursorResult executionResult);

}
