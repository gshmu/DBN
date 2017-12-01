package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum ConnectionProperty implements Property {
    ACTIVE,
    RESERVED,
    AUTO_COMMIT,
    COMMITTING,
    ROLLING_BACK,
    RESOLVING_TRANSACTION;

    private int idx;

    ConnectionProperty() {
        this.idx = PropertyHolderImpl.idx(this);
    }

    @Override
    public int idx() {
        return idx;
    }
}
