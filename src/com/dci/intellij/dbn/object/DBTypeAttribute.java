package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;

public interface DBTypeAttribute extends DBOrderedObject {
    DBType getType();
    DBDataType getDataType();
}
