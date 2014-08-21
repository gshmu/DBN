package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import gnu.trove.THashSet;

public class ElementLookupContext {
    private Set<NamedElementType> scannedElements = new THashSet<NamedElementType>();
    protected Set<String> branches;
    protected double languageVersion = 9999;

    public ElementLookupContext() {}

    public ElementLookupContext(Set<String> branches) {
        this.branches = branches;
    }

    @Nullable
    public Set<String> getBranches() {
        return branches;
    }

    private boolean checkBranches(ElementTypeRef elementTypeRef) {
        ElementType parent = elementTypeRef.getElementType().getParent();
        boolean checkBranches = parent != null && parent.hasBranchChecks();
        return !checkBranches || branches == null || elementTypeRef.supportsBranches(branches);
    }

    private boolean checkVersion(ElementTypeRef elementTypeRef) {
        return languageVersion > elementTypeRef.getVersion();
    }

    public boolean check(ElementTypeRef elementTypeRef) {
        return checkVersion(elementTypeRef) && checkBranches(elementTypeRef);
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
