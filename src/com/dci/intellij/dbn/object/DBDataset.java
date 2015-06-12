package com.dci.intellij.dbn.object;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBDataset extends DBSchemaObject {
    @Nullable List<DBColumn> getColumns();
    @Nullable DBColumn getColumn(String name);

    @Nullable List<DBConstraint> getConstraints();
    @Nullable DBConstraint getConstraint(String name);

    @Nullable List<DBDatasetTrigger> getTriggers();
    @Nullable DBDatasetTrigger getTrigger(String name);

    @Nullable List<DBIndex> getIndexes();
    @Nullable DBIndex getIndex(String name);

    boolean hasLobColumns();
}