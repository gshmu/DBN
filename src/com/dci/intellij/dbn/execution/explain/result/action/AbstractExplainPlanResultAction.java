package com.dci.intellij.dbn.execution.explain.result.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractExplainPlanResultAction extends DumbAwareProjectAction {
    AbstractExplainPlanResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    private static ExplainPlanResult getExplainPlanResult(AnActionEvent e) {
        ExplainPlanResult result = e.getData(DataKeys.EXPLAIN_PLAN_RESULT);
        if (result == null) {
            Project project = e.getProject();
            if (project != null) {
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
                if (executionResult instanceof ExplainPlanResult) {
                    return (ExplainPlanResult) executionResult;
                }
            }
        }
        return result;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        if (Failsafe.check(explainPlanResult)) {
            actionPerformed(e, project, explainPlanResult);
        }
    }

    @Override
    protected final void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(explainPlanResult != null);
        update(e, project, explainPlanResult);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ExplainPlanResult explainPlanResult);

    protected abstract void update(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @Nullable ExplainPlanResult explainPlanResult);
}
