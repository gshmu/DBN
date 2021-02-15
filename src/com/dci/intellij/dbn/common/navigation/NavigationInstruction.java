package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.Property;

public enum NavigationInstruction implements Property{
    OPEN,
    FOCUS,
    SCROLL,
    SELECT,
    RESET;

    private final long index = Property.idx(this);

    @Override
    public long index() {
        return index;
    }
}
