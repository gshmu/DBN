package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionStatus implements Property.IntBase {
    QUEUED,
    PROMPTED,
    EXECUTING,
    CANCELLED;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
