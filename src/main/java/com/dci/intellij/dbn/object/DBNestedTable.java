package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;

import java.util.List;

public interface DBNestedTable extends DBObject {
    List<DBNestedTableColumn> getColumns();
    DBNestedTableColumn getColumn(String name);

    DBTable getTable();
}