package com.dci.intellij.dbn.data.type;

import org.jetbrains.annotations.Nullable;

public class BasicDataTypeDefinition implements DataTypeDefinition {
    private GenericDataType genericDataType;
    private String name;
    private Class typeClass;
    private int sqlType;
    private boolean pseudoNative = false;


    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType) {
        this(name, typeClass, sqlType, genericDataType, false);
    }

    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType, boolean pseudoNative) {
        this.name = name;
        this.typeClass = typeClass;
        this.sqlType = sqlType;
        this.genericDataType = genericDataType;
        this.pseudoNative = pseudoNative;
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

    @Override
    public boolean isPseudoNative() {
        return pseudoNative;
    }

    public GenericDataType getGenericDataType() {
        return genericDataType;
    }

    @Override
    public String toString() {
        return "[NAME = " + name + ", GENERIC_TYPE = " + genericDataType + " TYPE_CLASS = " + typeClass + " SQL_TYPE = " + sqlType + "]";
    }

    public Object convert(@Nullable Object object) {
        return object;
    }
}