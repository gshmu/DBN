package com.dci.intellij.dbn.editor.data.model;


import com.dci.intellij.dbn.common.property.Property;

public enum RecordStatus implements Property {
    INSERTING,
    UPDATING,

    INSERTED,
    DELETED,
    MODIFIED,

    DIRTY,
    DISPOSED,
    ;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
