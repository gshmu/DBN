package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilderProvider;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TokenElementType extends LeafElementType, LookupItemBuilderProvider {
    String SEPARATOR = "SEPARATOR";

    @Nullable
    String getText();

    boolean isCharacter();

    TokenTypeCategory getFlavor();

    TokenTypeCategory getTokenTypeCategory();

    List<TokenElementTypeChain> getPossibleTokenChains();

}
