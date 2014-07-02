package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

import java.util.Set;

public interface LeafElementType extends ElementType {
    void setTokenType(TokenType tokenType);

    TokenType getTokenType();

    void setOptional(boolean optional);

    boolean isOptional();

    void registerLeaf();

    boolean isIdentifier();

    boolean isSameAs(LeafElementType leaf);

    Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, CodeCompletionFilterSettings filterSettings);
}
