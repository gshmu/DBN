package com.dci.intellij.dbn.object.common.status;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DBObjectStatus implements Property {
    PRESENT(false, true),
    ENABLED(true, true),
    EDITABLE(false, false),
    VALID(true, true),
    DEBUG(true, true),
    COMPILING(false, false);

    private boolean propagable;
    private boolean defaultValue;

    DBObjectStatus(boolean propagable, boolean defaultValue) {
        this.propagable = propagable;
        this.defaultValue = defaultValue;
    }

    public boolean isPropagable() {
        return propagable;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int idx() {
        return PropertyHolderImpl.idx(this);
    }
}
