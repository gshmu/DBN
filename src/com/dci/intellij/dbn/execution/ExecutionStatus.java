package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionStatus implements Property.IntBase {
    QUEUED,
    PROMPTED,
    EXECUTING,
    CANCELLED;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computed() {
        return computed;
    }
}
