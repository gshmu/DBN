package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBNativeDataType;

import java.util.List;

public interface DBType<P extends DBTypeProcedure, F extends DBTypeFunction> extends DBProgram<P, F> {
    public static final int TYPECODE_TYPE = 0;
    public static final int TYPECODE_OBJECT = 1;
    public static final int TYPECODE_COLLECTION = 2;

    List<DBTypeAttribute> getAttributes();
    DBType getSuperType();
    List<DBType> getSubTypes();

    DBNativeDataType getNativeDataType();
    boolean isCollection();

}