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

    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) throws ParseException {
        if (context.isAlternative()) {
            return parseNew(parentNode, context);
        }

        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = stepIn(parentNode, context);

        elementType.sort();
        TokenType token = builder.getToken();

        if (token != null && !token.isChameleon()) {
            ElementTypeRef element = elementType.getFirstChild();
            while (element != null) {
                if (context.check(element) && shouldParseElement(element.getElementType(), node, context)) {
                    ParseResult result = element.getParser().parse(node, context);

                    if (result.isMatch()) {
                        return stepOut(node, context, result.getType(), result.getMatchedTokens());
                    }
                }
                element = element.getNext();
            }
        }
        return stepOut(node, context, ParseResultType.NO_MATCH, 0);
    }

    private ParseResult parseNew(ParsePathNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        ParsePathNode node = stepIn(parentNode, context);

        elementType.sort();
        TokenType token = builder.getToken();

        if (token != null && !token.isChameleon()) {
            Pair<ElementTypeRef, ParseResult> bestResult = null;
            ElementTypeRef element = elementType.getFirstChild();
            while (element != null) {
                if (context.check(element)) {
                    ParseResult result = element.getParser().parse(node, context);
                    Marker marker = builder.mark();

                    if (result.isFullMatch()) {
                        builder.markerDrop(marker);
                        return stepOut(node, context, result.getType(), result.getMatchedTokens());

                    } else if (result.isPartialMatch()) {
                        if (bestResult == null || result.isBetterThan(bestResult.second())) {
                            bestResult = Pair.of(element, result);
                        }
                    }
                    builder.markerRollbackTo(marker);
                }
                element = element.getNext();
            }

            if (bestResult != null) {
                ElementTypeRef bestElement = bestResult.first();
                ParseResult result = bestElement.getParser().parse(node, context);
                return stepOut(node, context, result.getType(), result.getMatchedTokens());
            }
        }
        return stepOut(node, context, ParseResultType.NO_MATCH, 0);
    }

}