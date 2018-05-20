package com.dci.intellij.dbn.object;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBTable extends DBDataset {
    boolean isTemporary();
    @Nullable
    List<DBIndex> getIndexes();
    List<DBNestedTable> getNestedTables();
    @Nullable
    DBIndex getIndex(String name);
    DBNestedTable getNestedTable(String name);
    List<DBColumn> getPrimaryKeyColumns();
    List<DBColumn> getForeignKeyColumns();
    List<DBColumn> getUniqueKeyColumns();
}