package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import gnu.trove.THashSet;

public class ElementLookupContext {
    private Set<NamedElementType> scannedElements = new THashSet<NamedElementType>();
    protected Set<Branch> branches;
    protected double languageVersion = 9999;

    public ElementLookupContext() {}

    public ElementLookupContext(Set<Branch> branches) {
        this.branches = branches;
    }

    public boolean check(ElementTypeRef elementTypeRef) {
        return elementTypeRef.check(branches, languageVersion);
    }

    public ElementLookupContext reset() {
        scannedElements.clear();
        return this;
    }

    public boolean isScanned(NamedElementType elementType) {
        return scannedElements.contains(elementType);
    }

    public void markScanned(NamedElementType elementType) {
        scannedElements.add(elementType);
    }
}
