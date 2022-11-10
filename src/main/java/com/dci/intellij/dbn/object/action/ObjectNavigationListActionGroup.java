package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.ObjectListProvider;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

import java.util.List;

public class ObjectNavigationListActionGroup extends DefaultActionGroup {
    public static final int MAX_ITEMS = 30;
    private final DBObjectNavigationList navigationList;
    private final DBObjectRef<?> parentObject;
    private final boolean showFullList;

    public ObjectNavigationListActionGroup(DBObject parentObject, DBObjectNavigationList navigationList, boolean showFullList) {
        super(navigationList.getName(), true);
        this.parentObject = DBObjectRef.of(parentObject);
        this.navigationList = navigationList;
        this.showFullList = showFullList;

        if (navigationList.getObject() != null) {
            add(new NavigateToObjectAction(parentObject, navigationList.getObject()));
        } else {
            List<DBObject> objects = getObjects();
            int itemsCount = showFullList ? (objects == null ? 0 : objects.size()) : MAX_ITEMS;
            buildNavigationActions(itemsCount);
        }
    }

    public DBObject getParentObject() {
        return DBObjectRef.get(parentObject);
    }

    private List<DBObject> getObjects() {
        List<DBObject> objects = navigationList.getObjects();
        if (objects == null) {
            ObjectListProvider objectsProvider = navigationList.getObjectsProvider();
            if (objectsProvider != null) {
                objects = objectsProvider.getObjects();
            }

        }
        return objects;
    }

    private void buildNavigationActions(int length) {
        List<DBObject> objects = getObjects();
        if (objects != null) {
            DBObject parentObject = getParentObject();
            for (int i=0; i<length; i++) {
                if (i == objects.size()) {
                    return;
                }
                DBObject object = objects.get(i);
                add(new NavigateToObjectAction(parentObject, object));
            }
            if (!showFullList && objects.size() > MAX_ITEMS) {
                add(new ObjectNavigationListShowAllAction(parentObject, navigationList));
            }
        }
    }
}
