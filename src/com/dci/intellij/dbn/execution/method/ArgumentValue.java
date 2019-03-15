package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;

public class ArgumentValue {
    private DBObjectRef<DBArgument> argumentRef;
    private DBObjectRef<DBTypeAttribute> attributeRef;
    private ArgumentValueHolder valueHolder;

    public ArgumentValue(@NotNull DBArgument argument, @Nullable DBTypeAttribute attributeRef, ArgumentValueHolder valueHolder) {
        this.argumentRef = DBObjectRef.from(argument);
        this.attributeRef = DBObjectRef.from(attributeRef);
        this.valueHolder = valueHolder;
    }

    public ArgumentValue(@NotNull DBArgument argument, ArgumentValueHolder valueHolder) {
        this.argumentRef = DBObjectRef.from(argument);
        this.valueHolder = valueHolder;
    }

    public ArgumentValueHolder getValueHolder() {
        return valueHolder;
    }

    public void setValueHolder(ArgumentValueHolder valueHolder) {
        this.valueHolder = valueHolder;
    }

    public DBObjectRef<DBArgument> getArgumentRef() {
        return argumentRef;
    }

    @Nullable
    public DBArgument getArgument() {
        return argumentRef.get();
    }

    public DBTypeAttribute getAttribute() {
        return DBObjectRef.get(attributeRef);
    }

    public String getName() {
        return
            attributeRef == null ?
                    argumentRef.objectName :
                    argumentRef.objectName + '.' + attributeRef.objectName;
    }

    public Object getValue() {
        return valueHolder.getValue();
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
        valueHolder.setValue(value);
    }

    public String toString() {
        return argumentRef.objectName + " = " + getValue();
    }

    public static <T> ArgumentValueHolder<T> createBasicValueHolder(T value) {
        ArgumentValueHolder<T> valueStore = new ArgumentValueHolder<T>() {
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
