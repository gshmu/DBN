package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;

public interface DBArgument extends DBOrderedObject {
    DBDataType getDataType();
    DBMethod getMethod();
    short getSequence();
    boolean isInput();
    boolean isOutput();
}