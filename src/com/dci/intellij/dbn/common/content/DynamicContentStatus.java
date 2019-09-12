package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.property.Property;

public enum DynamicContentStatus implements Property {
    MASTER,
    MUTABLE,
    INTERNAL,
    PASSIVE,

    DIRTY,
    ERROR,
    LOADED,

    CHANGING,
    REFRESHING,
    LOADING,
    LOADING_IN_BACKGROUND;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
