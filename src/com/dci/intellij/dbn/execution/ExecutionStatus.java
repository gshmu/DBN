package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionStatus implements Property.IntBase {
    QUEUED,
    PROMPTED,
    EXECUTING,
    CANCELLED;

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }
}
