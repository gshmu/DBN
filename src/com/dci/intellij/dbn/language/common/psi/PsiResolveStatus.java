package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum PsiResolveStatus implements Property{
    NEW,
    RESOLVING,
    CONNECTION_VALID,
    CONNECTION_ACTIVE;

    private final int index;

    PsiResolveStatus() {
        this.index = PropertyHolderImpl.idx(this);
    }

    @Override
    public int index() {
        return index;
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
