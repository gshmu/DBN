package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.lookup.Visitor;

public interface DBObjectListVisitor extends StatefulDisposable, Visitor<DBObjectList<?>> {
    void visit(DBObjectList<?> objectList);
}
