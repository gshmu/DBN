package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.Property;

public enum NavigationInstruction implements Property{
    OPEN,
    FOCUS,
    SCROLL;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
