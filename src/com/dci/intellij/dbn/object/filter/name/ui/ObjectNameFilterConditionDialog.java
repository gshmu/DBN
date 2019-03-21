package com.dci.intellij.dbn.object.filter.name.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.SimpleNameFilterCondition;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjectNameFilterConditionDialog extends DBNDialog<ObjectNameFilterConditionForm> {
    private CompoundFilterCondition parentCondition;
    private SimpleNameFilterCondition condition;
    private ConditionJoinType joinType;
    private DBObjectType objectType;
    private ObjectNameFilterConditionForm.Operation operation;

    public ObjectNameFilterConditionDialog(Project project, CompoundFilterCondition parentCondition, SimpleNameFilterCondition condition, DBObjectType objectType, ObjectNameFilterConditionForm.Operation operation) {
        super(project, getTitle(operation), true);
        this.condition = condition;
        this.parentCondition = parentCondition;
        this.objectType = objectType;
        this.operation = operation;
        setModal(true);
        setResizable(false);
        switch (operation) {
            case CREATE: getOKAction().putValue(Action.NAME, "Add"); break;
            case EDIT: getOKAction().putValue(Action.NAME, "Update"); break;
            case JOIN: getOKAction().putValue(Action.NAME, "Add"); break;
        }

        init();
    }

    @NotNull
    @Override
    protected ObjectNameFilterConditionForm createComponent() {
        return new ObjectNameFilterConditionForm(this, parentCondition, condition,  objectType, operation);
    }

    @Nullable
    private static String getTitle(ObjectNameFilterConditionForm.Operation operation) {
        return operation == ObjectNameFilterConditionForm.Operation.CREATE ? "Create filter" :
        operation == ObjectNameFilterConditionForm.Operation.EDIT ? "Edit filter condition" :
        operation == ObjectNameFilterConditionForm.Operation.JOIN ? "Join filter condition" : null;
    }

    @Override
    public void doOKAction() {
        ObjectNameFilterConditionForm component = getComponent();
        condition = component.getCondition();
        joinType = component.getJoinType();
        super.doOKAction();
    }

    @Override
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
