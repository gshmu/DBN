package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;

import java.util.List;

public class DBObjectRecursiveLoaderVisitor extends DisposableBase implements DBObjectListVisitor{
    public static final DBObjectRecursiveLoaderVisitor INSTANCE = new DBObjectRecursiveLoaderVisitor();

    private DBObjectRecursiveLoaderVisitor() {
    }

    @Override
    public void visitObjectList(DBObjectList<DBObject> objectList) {
        if (!objectList.getDependencyAdapter().isSubContent()) {
            List<DBObject> objects = objectList.getObjects();
            for (DBObject object : objects) {
                ProgressMonitor.checkCancelled();
                checkDisposed();

                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    childObjects.visitLists(this, false);
                }
            }
        }
    }
}
