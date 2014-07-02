package com.dci.intellij.dbn.language.common.element;

public interface OneOfElementType extends ElementType {
    void sort();

    ElementType[] getPossibleElementTypes();

    void warnAmbiguousBranches();
}
