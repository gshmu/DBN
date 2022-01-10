package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetLoadInstruction implements Property.IntBase {
    USE_CURRENT_FILTER,
    PRESERVE_CHANGES,
    DELIBERATE_ACTION,
    REBUILD;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
