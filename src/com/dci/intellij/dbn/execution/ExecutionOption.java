package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionOption implements Property.IntBase {
    ENABLE_LOGGING,
    COMMIT_AFTER_EXECUTION;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computed() {
        return computed;
    }}
