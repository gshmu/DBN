package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import gnu.trove.THashSet;

public class ElementLookupContext {
    public static double MAX_DB_VERSION = 9999;
    private Set<NamedElementType> scannedElements = new THashSet<NamedElementType>();
    protected Set<Branch> branches;
    protected double databaseVersion = MAX_DB_VERSION;

    @Deprecated
    public ElementLookupContext() {}

    public ElementLookupContext(double version) {
        this.databaseVersion = version;
    }

    public ElementLookupContext(Set<Branch> branches, double version) {
        this.branches = branches;
        this.databaseVersion = version;
    }

    public boolean check(ElementTypeRef elementTypeRef) {
        return elementTypeRef.check(branches, databaseVersion);
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
