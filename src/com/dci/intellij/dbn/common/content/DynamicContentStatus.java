package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.property.Property;

public enum DynamicContentStatus implements Property {
    DIRTY,
    DISPOSED,
    INTERNAL,
    CONCURRENT,
    LOADED,
    LOADING,
    CHANGING,
    MASTER;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
