package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.property.Property;

public enum ValueSelectorOption implements Property {
    HIDE_ICON,
    HIDE_DESCRIPTION;

    private final long index = Property.idx(this);

    @Override
    public long index() {
        return index;
    }

}
