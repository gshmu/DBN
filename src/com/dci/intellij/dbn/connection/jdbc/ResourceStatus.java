package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;

public enum ResourceStatus implements Property {
    // shared
    CLOSED,
    CLOSED_CHECKING,
    CLOSED_APPLYING,

    VALID,
    VALID_CHECKING,
    VALID_APPLYING,

    // statement
    CANCELLED,
    CANCELLED_CHECKING,
    CANCELLED_APPLYING,

    // connection
    AUTO_COMMIT,
    AUTO_COMMIT_CHECKING,
    AUTO_COMMIT_APPLYING,

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
