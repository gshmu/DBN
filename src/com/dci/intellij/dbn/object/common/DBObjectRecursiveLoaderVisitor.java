package com.dci.intellij.dbn.object.common;

import java.util.List;

import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;

public class DBObjectRecursiveLoaderVisitor implements DBObjectListVisitor{
    public static final DBObjectRecursiveLoaderVisitor INSTANCE = new DBObjectRecursiveLoaderVisitor();

    private DBObjectRecursiveLoaderVisitor() {
    }

    @Override
    public void visitObjectList(DBObjectList<DBObject> objectList) {
        if (!objectList.getDependencyAdapter().isSubContent()) {
            List<DBObject> objects = objectList.getObjects();
            for (DBObject object : objects) {
                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    childObjects.visitLists(this, false);
                }
            }
        }
    }
}
