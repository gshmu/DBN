package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.property.Property;

enum ConnectionSettingsStatus implements Property.IntBase {
    NEW,
    ACTIVE,
    SIGNED;

    static final ConnectionSettingsStatus[] VALUES = values();

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }
}
