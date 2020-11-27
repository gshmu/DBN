package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.database.common.util.DataTypeParseAdapter;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class BasicDataTypeDefinition implements DataTypeDefinition {
    private final @Getter GenericDataType genericDataType;
    private final @Getter String name;
    private final @Getter Class typeClass;
    private final @Getter int sqlType;
    private final @Getter boolean pseudoNative;
    private final @Getter String contentTypeName;
    private @Getter @Setter DataTypeParseAdapter parseAdapter;


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

    @Override
    public String toString() {
        return "[NAME = " + name + ", GENERIC_TYPE = " + genericDataType + ", TYPE_CLASS = " + typeClass.getName() + " SQL_TYPE = " + sqlType + ']';
    }

    @Override
    public Object convert(@Nullable Object object) {
        return object;
    }
}