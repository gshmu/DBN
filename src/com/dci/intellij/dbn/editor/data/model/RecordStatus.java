package com.dci.intellij.dbn.editor.data.model;


import com.dci.intellij.dbn.common.property.Property;

public enum RecordStatus implements Property.IntBase {
    INSERTING,
    UPDATING,

    INSERTED,
    DELETED,
    MODIFIED,

    DIRTY,
    DISPOSED,
    ;

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }
}
