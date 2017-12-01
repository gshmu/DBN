package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
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

    //statement



    // connection
    ACTIVE,
    RESERVED,
    AUTO_COMMIT,
    COMMITTING,
    ROLLING_BACK,
    RESOLVING_TRANSACTION;

    private int idx;

    ResourceStatus() {
        this.idx = PropertyHolderImpl.idx(this);
    }

    @Override
    public int index() {
        return idx;
    }
}
