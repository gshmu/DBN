package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WrapperElementTypeParser extends ElementTypeParser<WrapperElementType> {
    public WrapperElementTypeParser(WrapperElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.builder;
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        ElementTypeBase wrappedElement = elementType.getWrappedElement();
        TokenElementType beginTokenElement = elementType.getBeginTokenElement();
        TokenElementType endTokenElement = elementType.getEndTokenElement();

        int matchedTokens = 0;

        // parse begin token
        ParseResult beginTokenResult = beginTokenElement.getParser().parse(node, optional, depth + 1, context);

        TokenType beginTokenType = beginTokenElement.tokenType;
        TokenType endTokenType = endTokenElement.tokenType;
        boolean isStrong = elementType.isStrong();

        boolean beginMatched = beginTokenResult.isMatch() || (builder.lookBack(1) == beginTokenType && !builder.isExplicitRange(beginTokenType));
        if (beginMatched) {
            matchedTokens++;
            boolean initialExplicitRange = builder.isExplicitRange(beginTokenType);
            builder.setExplicitRange(beginTokenType, true);

            ParseResult wrappedResult = wrappedElement.getParser().parse(node, false, depth -1, context);
            matchedTokens = matchedTokens + wrappedResult.matchedTokens;

            ParseResultType wrappedResultType = wrappedResult.type;
            if (wrappedResultType == ParseResultType.NO_MATCH  && !elementType.wrappedElementOptional) {
                if (!isStrong && builder.getTokenType() != endTokenType) {
                    builder.setExplicitRange(beginTokenType, initialExplicitRange);
                    return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
                } else {
                    Set<TokenType> possibleTokens = wrappedElement.getLookupCache().getFirstPossibleTokens();
                    ParseBuilderErrorHandler.updateBuilderError(possibleTokens, context);

                }
            }

            // check the end element => exit with partial match if not available
            ParseResult endTokenResult = endTokenElement.getParser().parse(node, false, depth - 1, context);
            if (endTokenResult.isMatch()) {
                matchedTokens++;
                return stepOut(node, context, depth, ParseResultType.FULL_MATCH, matchedTokens);
            } else {
                builder.setExplicitRange(beginTokenType, initialExplicitRange);
                return stepOut(node, context, depth, ParseResultType.PARTIAL_MATCH, matchedTokens);
            }
        }

        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private static boolean isParentWrapping(ParsePathNode node, TokenType tokenType) {
        ParsePathNode parent = node.parent;
        while (parent != null && parent.getCursorPosition() == 0) {
            WrappingDefinition parentWrapping = parent.elementType.getWrapping();
            if (parentWrapping != null && parentWrapping.getBeginElementType().tokenType == tokenType) {
                return true;
            }
            parent = parent.parent;
        }
        return false;
    }
}