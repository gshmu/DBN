package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObject;

public interface DBArgument extends DBObject, DBOrderedObject {
    DBDataType getDataType();
    DBMethod getMethod();
    short getSequence();
    boolean isInput();
    boolean isOutput();
}