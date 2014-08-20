package com.dci.intellij.dbn.language.common.element.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;

public class ElementTypeRef {
    private ElementType elementType;
    private boolean optional;
    private double version;
    private Set<String> supportedBranches;
    private Set<String> requiredBranches;

    public ElementTypeRef(ElementType elementType, boolean optional, double version, List<String> supportedBranches, List<String> requiredBranches) {
        this.elementType = elementType;
        this.optional = optional;
        this.version = version;
        this.supportedBranches = supportedBranches == null ? null : new HashSet<String>(supportedBranches);
        this.requiredBranches = requiredBranches == null ? null : new HashSet<String>(requiredBranches);
    }

    public ElementTypeRef(ElementType elementType, boolean optional, double version) {
        this.elementType = elementType;
        this.optional = optional;
        this.version = version;
    }

    public boolean supportsBranches(Set<String> branches) {
        return supportedBranches != null && supportedBranches.containsAll(branches);
    }

    public ElementType getElementType() {
        return elementType;
    }

    public boolean isOptional() {
        return optional;
    }

    public double getVersion() {
        return version;
    }

    public ElementTypeLookupCache getLookupCache() {
        return elementType.getLookupCache();
    }

    public ElementTypeParser getParser() {
        return elementType.getParser();
    }
}
