package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.type.DBObjectType;

public class DBTypeAttributePositionComparator extends DBObjectComparator<DBTypeAttribute> {
    public DBTypeAttributePositionComparator() {
        super(DBObjectType.TYPE_ATTRIBUTE, SortingType.POSITION);
    }

    @Override
    public int compare(DBTypeAttribute attribute1, DBTypeAttribute attribute2) {
        DBType type1 = attribute1.getType();
        DBType type2 = attribute2.getType();
        int result = compareObject(type1, type2);
        if (result == 0) {
            return comparePosition(attribute1, attribute2);
        }
        return result;
    }
}
