package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum ResourceStatus implements Property {
    //shared
    CLOSED,
    CLOSED_CHECKING,
    CLOSED_SETTING,

    INVALID,
    INVALID_SETTING,
    INVALID_CHECKING,

    CANCELLED,
    CANCELLED_SETTING,
    CANCELLED_CHECKING,

    AUTO_COMMIT,
    AUTO_COMMIT_SETTING,
    AUTO_COMMIT_CHECKING,

    // connection
    ACTIVE,
    RESERVED,
    COMMITTING,
    ROLLING_BACK,
    RESOLVING_TRANSACTION;

    private final int index = PropertyHolderImpl.idx(this);

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
