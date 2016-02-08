package com.dci.intellij.dbn.language.common.element;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilderProvider;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;

public interface TokenElementType extends LeafElementType, LookupItemBuilderProvider {
    String SEPARATOR = "SEPARATOR";

    @Nullable
    String getText();

    boolean isCharacter();

    TokenTypeCategory getFlavor();

    TokenTypeCategory getTokenTypeCategory();

    List<TokenElementTypeChain> getPossibleTokenChains();

}
