package com.dci.intellij.dbn.execution.explain.result.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractExplainPlanResultAction extends DumbAwareContextAction<ExplainPlanResult> {
    AbstractExplainPlanResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    protected ExplainPlanResult getTarget(@NotNull AnActionEvent e) {
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
}
