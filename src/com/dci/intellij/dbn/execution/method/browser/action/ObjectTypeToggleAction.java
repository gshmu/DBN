package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public class ObjectTypeToggleAction extends ToggleAction {
    private MethodExecutionBrowserForm browserComponent;
    private DBObjectType objectType;

    public ObjectTypeToggleAction(MethodExecutionBrowserForm browserComponent, DBObjectType objectType) {
        super("Show " + objectType.getListName(), null, objectType.getIcon());
        this.objectType = objectType;
        this.browserComponent = browserComponent;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return browserComponent.getSettings().getObjectVisibility(objectType);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        browserComponent.setObjectsVisible(objectType, state);
    }
}
