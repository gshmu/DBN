package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;

import java.util.List;

public interface DBType extends DBProgram<DBTypeProcedure, DBTypeFunction> {
    List<DBTypeAttribute> getAttributes();
    DBType getSuperType();
    DBDataType getCollectionElementType();
    List<DBType> getSubTypes();

    DBNativeDataType getNativeDataType();
    boolean isCollection();

}