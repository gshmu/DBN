package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.property.Property;

public enum DynamicContentStatus implements Property {
    MASTER,
    INTERNAL,
    CONCURRENT,

    DIRTY,
    LOADED,
    DISPOSED,

    CHANGING,
    LOADING,
    LOADING_IN_BACKGROUND;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
