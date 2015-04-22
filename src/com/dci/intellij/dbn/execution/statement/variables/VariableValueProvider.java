package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.data.type.GenericDataType;

public abstract class VariableValueProvider {
    public abstract String getValue();
    public abstract GenericDataType getDataType();
    public abstract boolean useNull();
}
