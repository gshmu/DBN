package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;

import java.util.List;

public class ReferencingObjectsListShowAction extends ObjectListShowAction {
    public ReferencingObjectsListShowAction(DBSchemaObject object) {
        super("Referencing objects", object);
    }

    public List<DBObject> getObjectList() {
        return ((DBSchemaObject) sourceObject).getReferencingObjects();
    }

    public String getTitle() {
        return "Objects referencing " + sourceObject.getQualifiedNameWithType();
    }

    public String getEmptyListMessage() {
        return "No references on " +  sourceObject.getQualifiedNameWithType() + " found";
    }

    public String getListName() {
       return "referencing objects";
    }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        return new NavigateToObjectAction(this.sourceObject, object);
    }
    
}