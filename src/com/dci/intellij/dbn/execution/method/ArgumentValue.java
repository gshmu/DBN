package com.dci.intellij.dbn.execution.method;

import java.sql.ResultSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class ArgumentValue {
    private DBObjectRef<DBArgument> argumentRef;
    private DBObjectRef<DBTypeAttribute> attributeRef;
    private ArgumentValueStore valueStore;

    public ArgumentValue(@NotNull DBArgument argument, @Nullable DBTypeAttribute attributeRef, ArgumentValueStore valueStore) {
        this.argumentRef = DBObjectRef.from(argument);
        this.attributeRef = DBObjectRef.from(attributeRef);
        this.valueStore = valueStore;
    }

    public ArgumentValue(@NotNull DBArgument argument, ArgumentValueStore valueStore) {
        this.argumentRef = DBObjectRef.from(argument);
        this.valueStore = valueStore;
    }

    public ArgumentValueStore getValueStore() {
        return valueStore;
    }

    public void setValueStore(ArgumentValueStore valueStore) {
        this.valueStore = valueStore;
    }

    public DBObjectRef<DBArgument> getArgumentRef() {
        return argumentRef;
    }

    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public DBTypeAttribute getAttribute() {
        return DBObjectRef.get(attributeRef);
    }

    public String getName() {
        return
            attributeRef == null ?
                    argumentRef.getObjectName() :
                    argumentRef.getObjectName() + '.' + attributeRef.getObjectName();
    }

    public Object getValue() {
        return valueStore.getValue();
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
        return getValue() instanceof ResultSet;
    }

    public void setValue(Object value) {
        valueStore.setValue(value);
    }

    public String toString() {
        return argumentRef.getObjectName() + " = " + getValue();
    }

    public static <T> ArgumentValueStore<T> createBasicStore(T value) {
        ArgumentValueStore<T> valueStore = new ArgumentValueStore<T>() {
            private T value;

            @Override
            public T getValue() {
                return value;
            }

            @Override
            public void setValue(T value) {
                this.value = value;
            }
        };

        valueStore.setValue(value);
        return valueStore;
    }
}
