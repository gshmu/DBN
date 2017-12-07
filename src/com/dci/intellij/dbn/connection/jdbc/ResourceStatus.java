package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum ResourceStatus implements Property {
    //shared
    CLOSED,
    CLOSING,
    CHECKING_CLOSED,

    INVALID,
    INVALIDATING,
    CHECKING_INVALID,

    CANCELLED,
    CANCELLING,
    CHECKING_CANCELLED,

    // connection
    ACTIVE,
    RESERVED,
    AUTO_COMMIT,
    COMMITTING,
    ROLLING_BACK,
    RESOLVING_TRANSACTION;

    private final int index;

    ResourceStatus() {
        this.index = PropertyHolderImpl.idx(this);
    }

    @Override
    public int index() {
        return index;
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
