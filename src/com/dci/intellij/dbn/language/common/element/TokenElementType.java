package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilderProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;

public interface TokenElementType extends LeafElementType, LookupItemBuilderProvider {
    String SEPARATOR = "SEPARATOR";

    boolean isCharacter();

    TokenTypeCategory getFlavor();

    TokenTypeCategory getTokenTypeCategory();

}
