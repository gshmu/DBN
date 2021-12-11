package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.common.Pair;
import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder.Marker;

public class OneOfElementTypeParser extends ElementTypeParser<OneOfElementType> {
    private static boolean scanMode = false;

    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) throws ParseException {
        if (scanMode) {
            return scanParse(parentNode, context);
        }

        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = stepIn(parentNode, context);

        elementType.sort();
        TokenType tokenType = builder.getTokenType();

        if (tokenType != null && !tokenType.isChameleon()) {
            ElementTypeRef child = elementType.getFirstChild();
            while (child != null) {
                if (context.check(child) && shouldParseElement(child.elementType, node, context)) {
                    ParseResult result = child.getParser().parse(node, context);

                    if (result.isMatch()) {
                        return stepOut(node, context, result.getType(), result.getMatchedTokens());
                    }
                }
                child = child.getNext();
            }
        }
        return stepOut(node, context, ParseResultType.NO_MATCH, 0);
    }

    private ParseResult scanParse(ParsePathNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = stepIn(parentNode, context);

        elementType.sort();
        TokenType tokenType = builder.getTokenType();

        if (tokenType != null && !tokenType.isChameleon()) {
            Marker marker = builder.mark();
            Pair<ElementTypeRef, ParseResult> bestResult = null;
            ElementTypeRef child = elementType.getFirstChild();
            while (child != null) {
                if (context.check(child) && shouldParseElement(child.elementType, node, context)) {
                    ParseResult result = child.getParser().parse(node, context);

                    if (result.isFullMatch()) {
                        marker.drop();
                        return stepOut(node, context, result.getType(), result.getMatchedTokens());
                    } else if (result.isPartialMatch()) {
                        if (bestResult == null || result.isBetterThan(bestResult.second())) {
                            bestResult = Pair.of(child, result);
                        }
                        builder.markerRollbackTo(marker);
                    }
                }
                child = child.getNext();
            }
            builder.markerRollbackTo(marker);

            if (bestResult != null) {
                ElementTypeRef element = bestResult.first();
                ParseResult result = element.getParser().parse(node, context);
                return stepOut(node, context, result.getType(), result.getMatchedTokens());
            }
        }
        return stepOut(node, context, ParseResultType.NO_MATCH, 0);
    }

}