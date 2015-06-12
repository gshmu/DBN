package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBIndex extends DBSchemaObject {
    boolean isUnique();
    DBDataset getDataset();

    List<DBColumn> getColumns();
}
