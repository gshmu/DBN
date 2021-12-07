package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.property.Property;

public enum DynamicContentStatus implements Property.IntBase {
    MASTER,
    MUTABLE,
    INTERNAL,
    PASSIVE,

    DIRTY,
    ERROR,
    LOADED,
    DISPOSED,

    CHANGING,
    REFRESHING,
    LOADING,
    LOADING_IN_BACKGROUND;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
