package com.dci.intellij.dbn.object.filter.quick.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.filter.quick.ui.ObjectQuickFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class EnableDisableQuickFilterConditionAction extends DumbAwareAction {
    private ObjectQuickFilterConditionForm conditionForm;

    public EnableDisableQuickFilterConditionAction(ObjectQuickFilterConditionForm conditionForm) {
        this.conditionForm = conditionForm;
    }

    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(
                conditionForm.isActive() ?
                        Icons.COMMON_FILTER_ACTIVE :
                        Icons.COMMON_FILTER_INACTIVE);
        e.getPresentation().setText(
                conditionForm.isActive() ?
                        "Deactivate Condition" :
                        "Activate Condition");
    }

    public void actionPerformed(AnActionEvent e) {
        conditionForm.setActive(!conditionForm.isActive());
    }
}
