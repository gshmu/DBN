package com.dci.intellij.dbn.execution.explain.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.explain.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExplainPlanResultCollapseAllAction extends AbstractExplainPlanResultAction {
    public ExplainPlanResultCollapseAllAction() {
        super("Collapse All", Icons.ACTION_COLLAPSE_ALL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        if (explainPlanResult != null && !explainPlanResult.isDisposed()) {
            explainPlanResult.getResultPanel().collapseAllNodes();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Collapse All");
    }
}
