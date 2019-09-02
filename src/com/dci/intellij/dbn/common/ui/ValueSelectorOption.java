package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.property.Property;

public enum ValueSelectorOption implements Property {
    HIDE_ICON,
    HIDE_DESCRIPTION;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }

}
