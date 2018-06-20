package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DynamicContentStatus implements Property {
    DIRTY,
    DISPOSED,
    INDEXED,
    INTERNAL,
    CONCURRENT,
    LOADED,
    LOADING,
    LOADING_IN_BACKGROUND;

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
