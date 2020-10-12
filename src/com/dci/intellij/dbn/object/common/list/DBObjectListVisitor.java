package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.object.common.DBObject;

public interface DBObjectListVisitor extends StatefulDisposable {
    void visitObjectList(DBObjectList<DBObject> objectList);
}
