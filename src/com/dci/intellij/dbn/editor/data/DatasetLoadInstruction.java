package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetLoadInstruction implements Property.IntBase {
    USE_CURRENT_FILTER,
    PRESERVE_CHANGES,
    DELIBERATE_ACTION,
    REBUILD;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computed() {
        return computed;
    }
}
