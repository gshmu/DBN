package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.database.common.util.DataTypeParseAdapter;
import org.jetbrains.annotations.Nullable;

public class BasicDataTypeDefinition implements DataTypeDefinition {
    private GenericDataType genericDataType;
    private String name;
    private Class typeClass;
    private int sqlType;
    private boolean pseudoNative = false;
    private String contentTypeName;
    private DataTypeParseAdapter parseAdapter;


    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType) {
        this(name, typeClass, sqlType, genericDataType, false);
    }

    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType, boolean pseudoNative) {
        this(name, typeClass, sqlType, genericDataType, pseudoNative, null);

    }

    public BasicDataTypeDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType, boolean pseudoNative, String contentTypeName) {
        this.name = name;
        this.typeClass = typeClass;
        this.sqlType = sqlType;
        this.genericDataType = genericDataType;
        this.pseudoNative = pseudoNative;
        this.contentTypeName = contentTypeName;
    }

    @Nullable
    public DataTypeParseAdapter getParseAdapter() {
        return parseAdapter;
    }

    public void setParseAdapter(DataTypeParseAdapter parseAdapter) {
        this.parseAdapter = parseAdapter;
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
        return "[NAME = " + name + ", GENERIC_TYPE = " + genericDataType + ", TYPE_CLASS = " + typeClass.getName() + " SQL_TYPE = " + sqlType + ']';
    }

    public Object convert(@Nullable Object object) {
        return object;
    }

    @Nullable
    @Override
    public String getContentTypeName() {
        return contentTypeName;
    }
}