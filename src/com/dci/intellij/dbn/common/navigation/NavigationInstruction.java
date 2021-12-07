package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.Property;

public enum NavigationInstruction implements Property.IntBase {
    OPEN,
    FOCUS,
    SCROLL,
    SELECT,
    RESET;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
