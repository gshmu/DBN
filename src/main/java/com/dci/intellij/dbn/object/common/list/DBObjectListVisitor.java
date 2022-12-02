package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.lookup.Visitor;

@FunctionalInterface
public interface DBObjectListVisitor extends Visitor<DBObjectList<?>> {
    void visit(DBObjectList<?> objectList);
}
