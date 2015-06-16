package com.dci.intellij.dbn.object.common.list.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ObjectListFilterAction extends AnAction {

    private DBObjectList objectList;

    public ObjectListFilterAction(DBObjectList objectList) {
        super("Quick Filter... ");
        this.objectList = objectList;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(project);
            quickFilterManager.openFilterDialog(objectList);
        }

    }
}