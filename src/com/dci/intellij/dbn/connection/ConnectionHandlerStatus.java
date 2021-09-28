package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;

public enum ConnectionHandlerStatus implements Property {
    CONNECTED,
    CLEANING,
    LOADING,
    ACTIVE,
    VALID,
    BUSY;
}
