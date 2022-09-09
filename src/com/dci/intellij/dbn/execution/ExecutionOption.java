package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionOption implements Property.IntBase {
    ENABLE_LOGGING,
    COMMIT_AFTER_EXECUTION;

    public static final ExecutionOption[] VALUES = values();

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }}
