package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectTypeFilterCreateAction extends AbstractObjectFilterAction {
    private DBObjectType objectType;

    ObjectTypeFilterCreateAction(DBObjectType objectType, ObjectNameFilterSettingsForm settingsForm) {
        super(objectType.getName().toUpperCase(), objectType.getIcon(), settingsForm);
        this.objectType = objectType;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ObjectNameFilterManager filterManager = ObjectNameFilterManager.getInstance(project);
        filterManager.createFilter(objectType, settingsForm);
    }
}
