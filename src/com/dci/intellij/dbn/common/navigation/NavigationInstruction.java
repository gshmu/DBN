package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.Property;

public enum NavigationInstruction implements Property.IntBase {
    OPEN,
    FOCUS,
    SCROLL,
    SELECT,
    RESET;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
