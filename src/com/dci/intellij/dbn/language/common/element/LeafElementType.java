package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface LeafElementType extends ElementType {
    void setTokenType(TokenType tokenType);

    void setOptional(boolean optional);

    boolean isOptional();

    void registerLeaf();

    boolean isIdentifier();

    boolean isSameAs(LeafElementType leaf);

    Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, @NotNull ElementLookupContext context);

    boolean isNextPossibleToken(TokenType tokenType, ParsePathNode pathNode, ParserContext context);

    boolean isNextRequiredToken(TokenType tokenType, ParsePathNode pathNode, ParserContext context);
}
