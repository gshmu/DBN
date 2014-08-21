package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import gnu.trove.THashSet;

public class ElementLookupContext {
    private Set<NamedElementType> scannedElements = new THashSet<NamedElementType>();
    protected Set<String> branches;

    public ElementLookupContext() {}

    public ElementLookupContext(Set<String> branches) {
        this.branches = branches;
    }

    @Nullable
    public Set<String> getBranches() {
        return branches;
    }

    public boolean checkBranches(ElementTypeRef elementTypeRef) {
        return branches == null || elementTypeRef.supportsBranches(branches);
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
