package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;

public class NestedRangeElementType extends BasicElementTypeImpl {
    public NestedRangeElementType(ElementTypeBundle bundle) {
        super(bundle, "nested-range", "");
    }

    @Override
    public String toString() {
        return "nested-range";
    }
}
