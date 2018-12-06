package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetLoadInstruction implements Property {
    USE_CURRENT_FILTER,
    PRESERVE_CHANGES,
    DELIBERATE_ACTION,
    REBUILD;


    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
