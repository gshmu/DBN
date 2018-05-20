package com.dci.intellij.dbn.object.dependency.action;

import com.dci.intellij.dbn.object.action.NavigateToObjectAction;
import com.dci.intellij.dbn.object.action.ObjectListShowAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;

import java.util.List;

public class ReferencingObjectsListShowAction extends ObjectListShowAction {
    public ReferencingObjectsListShowAction(DBSchemaObject object) {
        super("Referencing objects", object);
    }

    public List<? extends DBObject> getObjectList() {
        return ((DBSchemaObject) getSourceObject()).getReferencingObjects();
    }

    public String getTitle() {
        return "Objects referencing " + getSourceObject().getQualifiedNameWithType();
    }

    public String getEmptyListMessage() {
        return "No references on " +  getSourceObject().getQualifiedNameWithType() + " found";
    }

    public String getListName() {
       return "referencing objects";
    }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        return new NavigateToObjectAction(this.getSourceObject(), object);
    }
    
}