package com.dci.intellij.dbn.object.filter.name.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.SimpleNameFilterCondition;
import com.intellij.openapi.project.Project;

public class ObjectNameFilterConditionDialog extends DBNDialog<ObjectNameFilterConditionForm> {
    private SimpleNameFilterCondition condition;
    private ConditionJoinType joinType;

    public ObjectNameFilterConditionDialog(Project project, CompoundFilterCondition parentCondition, SimpleNameFilterCondition condition, DBObjectType objectType, ObjectNameFilterConditionForm.Operation operation) {
        super(project, getTitle(operation), true);
        component = new ObjectNameFilterConditionForm(this, parentCondition, condition,  objectType, operation);
        setModal(true);
        setResizable(false);
        switch (operation) {
            case CREATE: getOKAction().putValue(Action.NAME, "Add"); break;
            case EDIT: getOKAction().putValue(Action.NAME, "Update"); break;
            case JOIN: getOKAction().putValue(Action.NAME, "Add"); break;
        }

        init();
    }

    @Nullable
    private static String getTitle(ObjectNameFilterConditionForm.Operation operation) {
        return operation == ObjectNameFilterConditionForm.Operation.CREATE ? "Create filter" :
        operation == ObjectNameFilterConditionForm.Operation.EDIT ? "Edit filter condition" :
        operation == ObjectNameFilterConditionForm.Operation.JOIN ? "Join filter condition" : null;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
    }

    public void doOKAction() {
        condition = component.getCondition();
        joinType = component.getJoinType();
        super.doOKAction();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

    public SimpleNameFilterCondition getCondition() {
        return condition;
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }
}
