package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.property.Property;

public enum ValueSelectorOption implements Property {
    HIDE_ICON,
    HIDE_DESCRIPTION;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
