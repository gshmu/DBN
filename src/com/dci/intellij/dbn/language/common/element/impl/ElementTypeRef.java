package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;

public class ElementTypeRef {
    private ElementType elementType;
    private boolean optional;
    private double version;

    public ElementTypeRef(ElementType elementType, boolean optional, double version) {
        this.elementType = elementType;
        this.optional = optional;
        this.version = version;
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
