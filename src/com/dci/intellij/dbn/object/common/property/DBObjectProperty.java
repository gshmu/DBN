package com.dci.intellij.dbn.object.common.property;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DBObjectProperty implements Property {
    NAVIGABLE,
    EDITABLE,
    COMPILABLE,
    DISABLEABLE,
    REFERENCEABLE,
    SCHEMA_OBJECT;


    @Override
    public int idx() {
        return PropertyHolderImpl.idx(this);
    }
}
