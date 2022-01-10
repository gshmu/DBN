package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.property.Property;

public enum ConnectionProperty implements Property.IntBase {
    RS_TYPE_SCROLL_INSENSITIVE,
    RS_TYPE_FORWARD_ONLY;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
