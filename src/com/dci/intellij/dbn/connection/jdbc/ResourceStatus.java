package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;

public enum ResourceStatus implements Property {
    // shared
    CLOSED,
    CLOSED_CHECKING,
    CLOSED_SETTING,

    VALID,
    VALID_SETTING,
    VALID_CHECKING,

    // statement
    CANCELLED,
    CANCELLED_SETTING,
    CANCELLED_CHECKING,

    // connection
    AUTO_COMMIT,
    AUTO_COMMIT_SETTING,
    AUTO_COMMIT_CHECKING,

    ACTIVE,
    RESERVED,
    COMMITTING,
    ROLLING_BACK,
    RESOLVING_TRANSACTION;

    private final long index = Property.idx(this);

    @Override
    public long index() {
        return index;
    }
}
