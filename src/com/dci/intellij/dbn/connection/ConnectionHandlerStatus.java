package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;

public enum ConnectionHandlerStatus implements Property {
    CONNECTED,
    CLEANING,
    LOADING,
    ACTIVE,
    VALID,
    BUSY;

    private final long index = Property.idx(this);

    @Override
    public long index() {
        return index;
    }
}
