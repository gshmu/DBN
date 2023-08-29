package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.type.DBObjectType;

public class DBTypeAttributeNameComparator extends DBObjectComparator<DBTypeAttribute> {
    public DBTypeAttributeNameComparator() {
        super(DBObjectType.TYPE_ATTRIBUTE, SortingType.NAME);
    }

    @Override
    public int compare(DBTypeAttribute attribute1, DBTypeAttribute attribute2) {
        DBType type1 = attribute1.getType();
        DBType type2 = attribute2.getType();
        int result = compareRef(type1, type2);
        if (result == 0) {
            return compareName(attribute1, attribute2);
        }

        return result;
    }
}
