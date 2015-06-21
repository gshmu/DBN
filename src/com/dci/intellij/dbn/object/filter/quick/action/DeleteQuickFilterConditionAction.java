package com.dci.intellij.dbn.object.filter.quick.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.filter.quick.ui.ObjectQuickFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DeleteQuickFilterConditionAction extends DumbAwareAction {
    private ObjectQuickFilterConditionForm conditionForm;

    public DeleteQuickFilterConditionAction(ObjectQuickFilterConditionForm conditionForm) {
        this.conditionForm = conditionForm;
    }

    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(Icons.DATASET_FILTER_CONDITION_REMOVE);
        e.getPresentation().setText("Remove Condition");
    }

    public void actionPerformed(AnActionEvent e) {
        conditionForm.remove();
    }

}
