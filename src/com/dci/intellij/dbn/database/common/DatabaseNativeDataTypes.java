package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.data.type.BasicDataTypeDefinition;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.data.type.DateTimeDataTypeDefinition;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.data.type.LiteralDataTypeDefinition;
import com.dci.intellij.dbn.data.type.NumericDataTypeDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class DatabaseNativeDataTypes {
    protected List<DataTypeDefinition> dataTypes = new ArrayList<DataTypeDefinition>();
    public List<DataTypeDefinition> list() {return dataTypes;}

    protected DataTypeDefinition createBasicDefinition(String name, Class typeClass, int sqlType, GenericDataType genericDataType) {
        BasicDataTypeDefinition dataTypeDefinition = new BasicDataTypeDefinition(name, typeClass, sqlType, genericDataType);
        dataTypes.add(dataTypeDefinition);
        return dataTypeDefinition;
    }

    protected DataTypeDefinition createLiteralDefinition(String name, Class typeClass, int sqlType) {
        BasicDataTypeDefinition dataTypeDefinition = new LiteralDataTypeDefinition(name, typeClass, sqlType);
        dataTypes.add(dataTypeDefinition);
        return dataTypeDefinition;
    }

    protected DataTypeDefinition createNumericDefinition(String name, Class typeClass, int sqlType) {
        BasicDataTypeDefinition dataTypeDefinition = new NumericDataTypeDefinition(name, typeClass, sqlType);
        dataTypes.add(dataTypeDefinition);
        return dataTypeDefinition;
    }

    protected DataTypeDefinition createDateTimeDefinition(String name, Class typeClass, int sqlType) {
        BasicDataTypeDefinition dataTypeDefinition = new DateTimeDataTypeDefinition(name, typeClass, sqlType);
        dataTypes.add(dataTypeDefinition);
        return dataTypeDefinition;
    }


}
