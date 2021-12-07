package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.property.Property;

public enum PsiResolveStatus implements Property.IntBase {
    NEW,
    RESOLVING,
    RESOLVING_OBJECT_TYPE,
    CONNECTION_VALID,
    CONNECTION_ACTIVE;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computedOrdinal() {
        return computed;
    }
}
