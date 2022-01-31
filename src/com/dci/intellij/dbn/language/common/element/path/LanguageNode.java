package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.common.path.Node;
import com.dci.intellij.dbn.language.common.element.ElementType;

public interface LanguageNode extends Node<ElementType> {

    @Override
    LanguageNode getParent();

    int getIndexInParent();
}
