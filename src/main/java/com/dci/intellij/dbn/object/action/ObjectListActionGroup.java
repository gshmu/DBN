package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import java.util.List;

public class ObjectListActionGroup extends DefaultActionGroup {

    private ObjectListShowAction listShowAction;
    private List<? extends DBObject> objects;
    private List<? extends DBObject> recentObjects;

    public ObjectListActionGroup(ObjectListShowAction listShowAction, List<? extends DBObject> objects, List<? extends DBObject> recentObjects) {
        super("", true);
        this.objects = objects;
        this.recentObjects = recentObjects;
        this.listShowAction = listShowAction;

        if (objects != null) {
            buildNavigationActions();
        }
    }

    private void buildNavigationActions() {
        if (recentObjects != null) {
            for (DBObject object : recentObjects) {
                add(listShowAction.createObjectAction(object));
            }
            add(Actions.SEPARATOR);
        }

        for (DBObject object : objects) {
            if (recentObjects == null || !recentObjects.contains(object))
            add(listShowAction.createObjectAction(object));
        }
    }
}