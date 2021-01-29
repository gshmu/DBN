package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.lookup.Visitor;
import com.dci.intellij.dbn.object.common.DBObject;

public interface DBObjectListVisitor extends StatefulDisposable, Visitor<DBObjectList<DBObject>> {
    void visit(DBObjectList<DBObject> objectList);
}
