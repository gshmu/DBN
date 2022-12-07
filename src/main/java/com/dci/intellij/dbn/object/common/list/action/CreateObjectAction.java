package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ref.WeakRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CreateObjectAction extends AnAction {

    private final WeakRef<DBObjectList> objectList;

    CreateObjectAction(DBObjectList objectList) {
        super("New " + objectList.getObjectType().getName() + "...");
        this.objectList = WeakRef.of(objectList);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBObjectList objectList = getObjectList();
        DBSchema schema = objectList.ensureParentEntity();
        Project project = schema.getProject();
        DatabaseObjectFactory factory = DatabaseObjectFactory.getInstance(project);
        factory.openFactoryInputDialog(schema, objectList.getObjectType());
    }

    @NotNull
    public DBObjectList getObjectList() {
        return Failsafe.nn(WeakRef.get(objectList));
    }
}