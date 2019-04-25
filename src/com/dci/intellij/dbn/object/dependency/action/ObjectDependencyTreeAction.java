package com.dci.intellij.dbn.object.dependency.action;

import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ObjectDependencyTreeAction extends AnObjectAction<DBSchemaObject> {
    public ObjectDependencyTreeAction(DBSchemaObject schemaObject) {
        super("Dependency Tree...", null, schemaObject);
        setDefaultIcon(true);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchemaObject object) {
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        dependencyManager.openDependencyTree(object);
    }
}