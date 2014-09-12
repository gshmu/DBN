package com.dci.intellij.dbn.language.common.element.parser.impl;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class WrapperElementTypeParser extends AbstractElementTypeParser<WrapperElementType> {
    public WrapperElementTypeParser(WrapperElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        boolean isWrappingOptional = getElementType().isWrappingOptional();
        ElementType wrappedElement = getElementType().getWrappedElement();
        TokenElementType beginTokenElement = getElementType().getBeginTokenElement();
        TokenElementType endTokenElement = getElementType().getEndTokenElement();

        int matchedTokens = 0;
        boolean isWrapped = false;

        // first try to parse the wrapped element directly, for supporting wrapped elements nesting
        if (isWrappingOptional) {
            ParseResult wrappedResult = wrappedElement.getParser().parse(node, optional, depth + 1, context);
            if (wrappedResult.isMatch()) {
                matchedTokens = matchedTokens + wrappedResult.getMatchedTokens();
                return stepOut(node, context, depth, wrappedResult.getType(), matchedTokens);
            } else {
                builder.markerRollbackTo(node.getElementMarker(), null);
                node = stepIn(parentNode, context);
            }
        }

        // parse begin token
        ParseResult beginTokenResult = beginTokenElement.getParser().parse(node, optional, depth + 1, context);

        TokenType beginTokenType = beginTokenElement.getTokenType();
        boolean beginMatched = beginTokenResult.isMatch() || (builder.lookBack(1) == beginTokenType && !builder.isExplicitRange(beginTokenType));
        if (beginMatched) {
            isWrapped = true;
            matchedTokens++;
        }

        if (beginMatched || isWrappingOptional) {
            ParseResult wrappedResult = wrappedElement.getParser().parse(node, false, depth -1, context);
            matchedTokens = matchedTokens + wrappedResult.getMatchedTokens();

            if (isWrapped) {
                // check the end element => exit with partial match if not available
                ParseResult endTokenResult = endTokenElement.getParser().parse(node, false, depth -1, context);
                if (endTokenResult.isMatch()) {
                    matchedTokens++;
                    return stepOut(node, context, depth, ParseResultType.FULL_MATCH, matchedTokens);
                } else {
                    return stepOut(node, context, depth, wrappedResult.getType(), matchedTokens);
                }
            } else {
                return stepOut(node, context, depth, wrappedResult.getType(), matchedTokens);
            }
        }

        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private boolean isParentWrapping(ParsePathNode node, TokenType tokenType) {
        ParsePathNode parent = node.getParent();
        while (parent != null && parent.getCursorPosition() == 0) {
            WrappingDefinition parentWrapping = parent.getElementType().getWrapping();
            if (parentWrapping != null && parentWrapping.getBeginElementType().getTokenType() == tokenType) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}