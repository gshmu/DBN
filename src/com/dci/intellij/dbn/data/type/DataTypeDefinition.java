package com.dci.intellij.dbn.data.type;

public interface DataTypeDefinition {
    String getName();
    Class getTypeClass();
    int getSqlType();
    GenericDataType getGenericDataType();
    Object convert(Object object);
}
