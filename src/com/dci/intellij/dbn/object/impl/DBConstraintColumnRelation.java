package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;

public class DBConstraintColumnRelation extends DBObjectRelationImpl<DBConstraint, DBColumn> {
    private short position;
    DBConstraintColumnRelation(DBConstraint constraint, DBColumn column, short position) {
        super(DBObjectRelationType.CONSTRAINT_COLUMN, constraint, column);
        this.position = position;
    }

    public short getPosition() {
        return position;
    }

    public DBConstraint getConstraint() {
        return getSourceObject();
    }

    public DBColumn getColumn() {
        return getTargetObject();
    }
}
