package com.dci.intellij.dbn.object.dependency.action;

import com.dci.intellij.dbn.object.action.NavigateToObjectAction;
import com.dci.intellij.dbn.object.action.ObjectListShowAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;

import java.util.List;

public class ReferencedObjectsListShowAction extends ObjectListShowAction {
    public ReferencedObjectsListShowAction(DBSchemaObject object) {
        super("Referenced objects", object);
    }

    @Override
    public List<DBObject> getObjectList() {
        return ((DBSchemaObject) getSourceObject()).getReferencedObjects();
    }

    @Override
    public String getTitle() {
        return "Objects referenced by " + getSourceObject().getQualifiedNameWithType();
    }

    @Override
    public String getEmptyListMessage() {
        return "No referenced objects found for " + getSourceObject().getQualifiedNameWithType();
    }


    @Override
    public String getListName() {
       return "referenced objects";
   }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        return new NavigateToObjectAction(this.getSourceObject(), object);
    }

}