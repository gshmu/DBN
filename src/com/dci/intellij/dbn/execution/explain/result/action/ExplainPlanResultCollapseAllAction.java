package com.dci.intellij.dbn.execution.explain.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.explain.result.ui.ExplainPlanResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExplainPlanResultCollapseAllAction extends AbstractExplainPlanResultAction {
    public ExplainPlanResultCollapseAllAction() {
        super("Collapse All", Icons.ACTION_COLLAPSE_ALL);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        if (Failsafe.check(explainPlanResult)) {
            ExplainPlanResultForm resultForm = explainPlanResult.getForm();
            if (Failsafe.check(resultForm)) {
                resultForm.collapseAllNodes();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        getTemplatePresentation().setText("Collapse All");
    }
}
