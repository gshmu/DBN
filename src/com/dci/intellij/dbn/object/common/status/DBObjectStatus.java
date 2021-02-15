package com.dci.intellij.dbn.object.common.status;

import com.dci.intellij.dbn.common.property.Property;

public enum DBObjectStatus implements Property {
    PRESENT(false, true),
    ENABLED(true, true),
    EDITABLE(false, false),
    VALID(true, true),
    DEBUG(true, true),
    COMPILING(false, false);

    private final long index = Property.idx(this);
    private final boolean propagable;
    private final boolean defaultValue;

    DBObjectStatus(boolean propagable, boolean defaultValue) {
        this.propagable = propagable;
        this.defaultValue = defaultValue;
    }

    @Override
    public long index() {
        return index;
    }

    public boolean isPropagable() {
        return propagable;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
