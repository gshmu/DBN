package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.OldTokenPairMonitor;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParserNode;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;

import java.util.Set;

public class WrapperElementTypeParser extends ElementTypeParser<WrapperElementType> {
    public WrapperElementTypeParser(WrapperElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParserNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        ParserNode node = stepIn(parentNode, context);

        ElementTypeBase wrappedElement = elementType.getWrappedElement();
        TokenElementType beginTokenElement = elementType.getBeginTokenElement();
        TokenElementType endTokenElement = elementType.getEndTokenElement();

        int matchedTokens = 0;

        // parse begin token
        ParseResult beginTokenResult = beginTokenElement.getParser().parse(node, context);

        TokenType beginTokenType = beginTokenElement.getTokenType();
        TokenType endTokenType = endTokenElement.getTokenType();
        boolean isStrong = elementType.isStrong();

        OldTokenPairMonitor tokenPairMonitor = builder.getTokenPairMonitor();
        boolean beginMatched = beginTokenResult.isMatch() || (builder.getPreviousToken() == beginTokenType && !tokenPairMonitor.isExplicitRange(beginTokenType));
        if (beginMatched) {
            matchedTokens++;
            boolean initialExplicitRange = tokenPairMonitor.isExplicitRange(beginTokenType);
            tokenPairMonitor.setExplicitRange(beginTokenType, true);

            ParseResult wrappedResult = wrappedElement.getParser().parse(node, context);
            matchedTokens = matchedTokens + wrappedResult.getMatchedTokens();

            ParseResultType wrappedResultType = wrappedResult.getType();
            if (wrappedResultType == ParseResultType.NO_MATCH  && !elementType.wrappedElementOptional) {
                if (!isStrong && builder.getToken() != endTokenType) {
                    tokenPairMonitor.setExplicitRange(beginTokenType, initialExplicitRange);
                    return stepOut(node, context, ParseResultType.NO_MATCH, matchedTokens);
                } else {
                    Set<TokenType> possibleTokens = wrappedElement.getLookupCache().getFirstPossibleTokens();
                    ParseBuilderErrorHandler.updateBuilderError(possibleTokens, context);

                }
            }

            // check the end element => exit with partial match if not available
            ParseResult endTokenResult = endTokenElement.getParser().parse(node, context);
            if (endTokenResult.isMatch()) {
                matchedTokens++;
                return stepOut(node, context, ParseResultType.FULL_MATCH, matchedTokens);
            } else {
                tokenPairMonitor.setExplicitRange(beginTokenType, initialExplicitRange);
                return stepOut(node, context, ParseResultType.PARTIAL_MATCH, matchedTokens);
            }
        }

        return stepOut(node, context, ParseResultType.NO_MATCH, matchedTokens);
    }

    private static boolean isParentWrapping(ParserNode node, TokenType tokenType) {
        ParserNode parent = node.getParent();
        while (parent != null && parent.getCursorPosition() == 0) {
            WrappingDefinition parentWrapping = parent.getElement().getWrapping();
            if (parentWrapping != null && parentWrapping.getBeginElementType().getTokenType() == tokenType) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}