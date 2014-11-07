package com.dci.intellij.dbn.data.type;

import org.jetbrains.annotations.Nullable;

public class BasicDataTypeDefinition implements DataTypeDefinition {
    private GenericDataType genericDataType;
    private String name;
    private Class typeClass;
    private int sqlType;


    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType) {
        this.name = name;
        this.typeClass = typeClass;
        this.sqlType = sqlType;
        this.genericDataType = genericDataType;
    }

    public String getName() {
        return name;
    }

    public Class getTypeClass() {
        return typeClass;
    }

    public int getSqlType() {
        return sqlType;
    }

    public GenericDataType getGenericDataType() {
        return genericDataType;
    }

    @Override
    public String toString() {
        return name;
    }

    public Object convert(@Nullable Object object) {
        return object;
    }
}