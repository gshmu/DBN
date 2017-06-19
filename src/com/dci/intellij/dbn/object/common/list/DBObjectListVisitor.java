package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.object.common.DBObject;

public interface DBObjectListVisitor extends Disposable{
    void visitObjectList(DBObjectList<DBObject> objectList);
}
