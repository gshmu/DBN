package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DropObjectAction extends DumbAwareAction {
    private DBObjectRef<DBSchemaObject> objectRef;

    public DropObjectAction(DBSchemaObject object) {
        super("Drop...", null, Icons.ACTION_DELETE);
        objectRef = DBObjectRef.from(object);
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.getnn(objectRef);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject object = getObject();
        DatabaseObjectFactory objectFactory = DatabaseObjectFactory.getInstance(object.getProject());
        objectFactory.dropObject(object);
    }
}