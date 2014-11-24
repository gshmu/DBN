package com.dci.intellij.dbn.execution.explain.result.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExplainPlanResultExpandAllAction extends AbstractExplainPlanResultAction {
    public ExplainPlanResultExpandAllAction() {
        super("Expand All", Icons.ACTION_EXPAND_ALL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        if (explainPlanResult != null && !explainPlanResult.isDisposed()) {
            explainPlanResult.getForm().expandAllNodes();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Expand All");
    }
}
