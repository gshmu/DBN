package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;

public class DBIndexColumnRelation extends DBObjectRelationImpl<DBIndex, DBColumn> {
    DBIndexColumnRelation(DBIndex index, DBColumn column) {
        super(DBObjectRelationType.INDEX_COLUMN, index, column);
    }

    public DBIndex getIndex() {
        return getSourceObject();
    }

    public DBColumn getColumn() {
        return getTargetObject();
    }
}