package com.dci.intellij.dbn.object.common.status;

import com.dci.intellij.dbn.common.property.Property;

public enum DBObjectStatus implements Property.IntBase {
    PRESENT(false, true),
    ENABLED(true, true),
    EDITABLE(false, false),
    VALID(true, true),
    DEBUG(true, true),
    COMPILING(false, false);

    private final Masks masks = new Masks(this);
    private final boolean propagable;
    private final boolean defaultValue;

    DBObjectStatus(boolean propagable, boolean defaultValue) {
        this.propagable = propagable;
        this.defaultValue = defaultValue;
    }

    @Override
    public Masks masks() {
        return masks;
    }

    public boolean isPropagable() {
        return propagable;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
