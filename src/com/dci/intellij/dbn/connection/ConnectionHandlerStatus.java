package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum ConnectionHandlerStatus implements Property {
    CONNECTED,
    CLEANING,
    LOADING,
    ACTIVE,
    VALID,
    BUSY;

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
