package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

import java.util.List;

public interface DBDataset extends DBSchemaObject {
    List<DBColumn> getColumns();
    DBColumn getColumn(String name);

    List<DBConstraint> getConstraints();
    DBConstraint getConstraint(String name);

    List<DBTrigger> getTriggers();
    DBTrigger getTrigger(String name);

    boolean hasLobColumns();
}