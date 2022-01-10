package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.property.Property;

public enum PsiResolveStatus implements Property.IntBase {
    NEW,
    RESOLVING,
    RESOLVING_OBJECT_TYPE,
    CONNECTION_VALID,
    CONNECTION_ACTIVE;

    private final Masks masks = new Masks(this);

    @Override
    public Masks masks() {
        return masks;
    }
}
