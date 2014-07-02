package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBArgumentRef;

public interface DBArgument extends DBObject {
    DBDataType getDataType();
    DBMethod getMethod();
    int getOverload();
    int getPosition();
    int getSequence();
    boolean isInput();
    boolean isOutput();

    @Override
    DBArgumentRef getRef();
}