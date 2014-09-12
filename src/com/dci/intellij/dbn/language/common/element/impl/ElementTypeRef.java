package com.dci.intellij.dbn.language.common.element.impl;

import java.util.Set;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;

public class ElementTypeRef {
    private ElementType parentElementType;
    private ElementType elementType;
    private boolean optional;
    private double version;
    private Set<Branch> supportedBranches;

    private ElementTypeRef previous;
    private ElementTypeRef next;

    public ElementTypeRef(ElementType parentElementType, ElementType elementType, boolean optional, double version, Set<Branch> supportedBranches) {
        this.parentElementType = parentElementType;
        this.elementType = elementType;
        this.optional = optional;
        this.version = version;
        this.supportedBranches = supportedBranches;
    }

    public boolean check(Set<Branch> branches, double currentVersion) {
        if (getVersion() > currentVersion) {
            return false;
        }

        if (branches != null) {
            Set<Branch> checkedBranches = getParentElementType().getCheckedBranches();
            if (checkedBranches != null) {
                if (supportedBranches != null) {
                    for (Branch branch : branches) {
                        if (checkedBranches.contains(branch)) {
                            for (Branch supportedBranch : supportedBranches) {
                                if (supportedBranch.equals(branch) && currentVersion >= supportedBranch.getVersion()) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public ElementType getParentElementType() {
        return parentElementType;
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

    public ElementTypeRef getPrevious() {
        return previous;
    }

    public void setPrevious(ElementTypeRef previous) {
        if (previous != null) {
            previous.setNext(this);
            this.previous = previous;
        }
    }

    public ElementTypeRef getNext() {
        return next;
    }

    public void setNext(ElementTypeRef next) {
        this.next = next;
    }
}
