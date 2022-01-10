package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.property.Property;

public enum ValueSelectorOption implements Property.IntBase {
    HIDE_ICON,
    HIDE_DESCRIPTION;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
