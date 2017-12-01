package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum ExecutionStatus implements Property {
    QUEUED,
    PROMPTED,
    EXECUTING,
    CANCELLED;

    @Override
    public int index() {
        return PropertyHolderImpl.idx(this);
    }

    @Override
    public PropertyGroup group() {
        return null;
    }

    @Override
    public boolean implicit() {
        return false;
    }
}
