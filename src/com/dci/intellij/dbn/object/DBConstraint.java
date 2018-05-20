package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DBConstraint extends DBSchemaObject {
    int DEFAULT = 0;
    int CHECK = 1;
    int PRIMARY_KEY = 2;
    int UNIQUE_KEY = 3;
    int FOREIGN_KEY = 4;
    int VIEW_CHECK = 5;
    int VIEW_READONLY = 6;
    
    int getConstraintType();
    boolean isPrimaryKey();
    boolean isForeignKey();
    boolean isUniqueKey();
    DBDataset getDataset();

    @Nullable
    DBConstraint getForeignKeyConstraint();

    List<DBColumn> getColumns();
    int getColumnPosition(DBColumn constraint);
    @Nullable DBColumn getColumnForPosition(int position);
}