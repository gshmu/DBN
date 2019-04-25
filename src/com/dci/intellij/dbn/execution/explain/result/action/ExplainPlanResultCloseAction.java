package com.dci.intellij.dbn.execution.explain.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplainPlanResultCloseAction extends AbstractExplainPlanResultAction {
    public ExplainPlanResultCloseAction() {
        super("Close", Icons.EXEC_RESULT_CLOSE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ExplainPlanResult explainPlanResult) {
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.removeResultTab(explainPlanResult);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable ExplainPlanResult explainPlanResult) {

    }
}
