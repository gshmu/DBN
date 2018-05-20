package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;

import java.util.List;

public interface DBType<P extends DBTypeProcedure, F extends DBTypeFunction> extends DBProgram<P, F> {
    List<DBTypeAttribute> getAttributes();
    DBType getSuperType();
    DBDataType getCollectionElementType();
    List<DBType> getSubTypes();

    DBNativeDataType getNativeDataType();
    boolean isCollection();

}