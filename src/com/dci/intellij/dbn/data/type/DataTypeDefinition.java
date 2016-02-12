package com.dci.intellij.dbn.data.type;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.database.common.util.DataTypeParseAdapter;

public interface DataTypeDefinition {
    @Nullable
    DataTypeParseAdapter getParseAdapter();

    String getName();
    Class getTypeClass();
    int getSqlType();

    boolean isPseudoNative();

    GenericDataType getGenericDataType();
    Object convert(@Nullable Object object);
    @Nullable String getContentTypeName();
}
