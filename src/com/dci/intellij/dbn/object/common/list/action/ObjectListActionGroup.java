package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ObjectListActionGroup extends DefaultActionGroup {

    public ObjectListActionGroup(DBObjectList objectList) {
        add(new ReloadObjectsAction(objectList));
        GenericDatabaseElement parentElement = objectList.getParentElement();
        if(parentElement instanceof DBSchema) {
            add (new ObjectListFilterAction(objectList));
            addSeparator();
            add (new CreateObjectAction(objectList));
        } else if (parentElement instanceof DBObjectBundle) {
            add (new ObjectListFilterAction(objectList));
            if (objectList.getObjectType() == DBObjectType.SCHEMA) {
                add (new HideEmptySchemasToggleAction(objectList.getConnectionHandler()));
            }
        }
    }
}