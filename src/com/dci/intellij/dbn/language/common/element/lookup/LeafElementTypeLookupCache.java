package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.element.LeafElementType;

public abstract class LeafElementTypeLookupCache<T extends LeafElementType> extends AbstractElementTypeLookupCache<T> {
    public LeafElementTypeLookupCache(T leafElementType) {
        super(leafElementType);
        T elementType = getElementType();
        allPossibleLeafs.add(elementType);
        firstPossibleLeafs.add(elementType);
        firstRequiredLeafs.add(elementType);
    }

    @Override
    public boolean containsLeaf(LeafElementType leafElementType) {
        return getElementType() == leafElementType;
    }

    @Override
    public boolean canStartWithLeaf(LeafElementType leafElementType) {
        return getElementType() == leafElementType;
    }
}