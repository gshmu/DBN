package com.dci.intellij.dbn.execution.explain.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.execution.explain.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class AbstractExplainPlanResultAction extends DumbAwareAction {
    protected AbstractExplainPlanResultAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public ExplainPlanResult getExplainPlanResult(AnActionEvent e) {
        return e.getData(DBNDataKeys.EXPLAIN_PLAN_RESULT);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        e.getPresentation().setEnabled(explainPlanResult != null);
    }

}
