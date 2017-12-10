package com.dci.intellij.dbn.object.common.status;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DBObjectStatus implements Property {
    PRESENT(false, true),
    ENABLED(true, true),
    EDITABLE(false, false),
    VALID(true, true),
    DEBUG(true, true),
    COMPILING(false, false);

    private final int index = PropertyHolderImpl.idx(this);
    private final boolean propagable;
    private final boolean defaultValue;

    DBObjectStatus(boolean propagable, boolean defaultValue) {
        this.propagable = propagable;
        this.defaultValue = defaultValue;
    }

    @Override
    public int index() {
        return index;
    }

    public boolean isPropagable() {
        return propagable;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public PropertyGroup group() {
        return null;
    }

    @Override
    public boolean implicit() {
        return false;
    }


}
