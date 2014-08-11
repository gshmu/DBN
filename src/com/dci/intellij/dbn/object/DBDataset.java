package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBDataset extends DBSchemaObject {
    List<DBColumn> getColumns();
    DBColumn getColumn(String name);

    List<DBConstraint> getConstraints();
    DBConstraint getConstraint(String name);

    List<DBTrigger> getTriggers();
    DBTrigger getTrigger(String name);

    boolean hasLobColumns();
}