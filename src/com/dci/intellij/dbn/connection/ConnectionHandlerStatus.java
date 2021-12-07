package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;

public enum ConnectionHandlerStatus implements Property.IntBase {
    CONNECTED,
    CLEANING,
    LOADING,
    ACTIVE,
    VALID,
    BUSY;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
