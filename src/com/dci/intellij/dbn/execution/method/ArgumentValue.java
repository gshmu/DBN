package com.dci.intellij.dbn.execution.method;

import java.sql.ResultSet;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class ArgumentValue {
    private DBObjectRef<DBArgument> argumentRef;
    private DBTypeAttribute attribute;
    private Object value;

    public ArgumentValue(DBArgument argument, DBTypeAttribute attribute, Object value) {
        this.argumentRef = argument.getRef();
        this.attribute = attribute;
        this.value = value;
    }

    public ArgumentValue(DBArgument argument, Object value) {
        this.argumentRef = argument.getRef();
        this.value = value;
    }

    public DBObjectRef<DBArgument> getArgumentRef() {
        return argumentRef;
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public DBTypeAttribute getAttribute() {
        return attribute;
    }

    public String getName() {
        return
            attribute == null ?
                    argumentRef.getObjectName() :
                    argumentRef.getObjectName() + "." + attribute.getName();
    }

    public Object getValue() {
        return value;
    }

    public boolean isLargeObject() {
        DBArgument argument = getArgument();
        if (argument != null) {
            DBDataType dataType = argument.getDataType();
            return dataType.isNative() && dataType.getNativeDataType().isLargeObject();
        }
        return false;
    }

    public boolean isCursor() {
        return value instanceof ResultSet;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return argumentRef.getObjectName() + " = " + value;
    }
}
