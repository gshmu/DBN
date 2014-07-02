package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.lookup.LookupItemFactory;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;

public interface TokenElementType extends LeafElementType {
    String SEPARATOR = "SEPARATOR";

    boolean isCharacter();

    TokenTypeCategory getFlavor();

    TokenTypeCategory getTokenTypeCategory();

    LookupItemFactory getLookupItemFactory(DBLanguage language);
}
