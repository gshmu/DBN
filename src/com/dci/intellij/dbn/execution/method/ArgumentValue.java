package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBArgumentRef;

import java.sql.ResultSet;

public class ArgumentValue {
    private DBArgumentRef argumentRef;
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

    public DBArgumentRef getArgumentRef() {
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
                    argumentRef.getName() :
                    argumentRef.getName() + "." + attribute.getName();
    }

    public Object getValue() {
        return value;
    }

    public boolean isCursor() {
        return value instanceof ResultSet;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return argumentRef.getName() + " = " + value;
    }
}
