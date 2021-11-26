package com.dci.intellij.dbn.language.common.element.parser.impl;

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
import org.jetbrains.annotations.NotNull;

public class OneOfElementTypeParser extends ElementTypeParser<OneOfElementType> {
    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.builder;
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        elementType.sort();
        TokenType tokenType = builder.getTokenType();

        if (tokenType != null && !tokenType.isChameleon()) {
            //PsiBuilder.Marker marker = builder.mark(null);
            ElementTypeRef child = elementType.getFirstChild();
            //Pair<ElementTypeRef, ParseResult> bestResult = null;
            while (child != null) {
                if (context.check(child) && shouldParseElement(child.elementType, node, context)) {
                    ParseResult result = child.getParser().parse(node, true, depth + 1, context);

                    //if (result.isFullMatch()) {
                    if (result.isMatch()) {
                        //marker.drop();
                        return stepOut(node, context, depth, result.getType(), result.getMatchedTokens());
                    } /*else if (result.isPartialMatch()) {
                        if (bestResult == null || result.isBetterThan(bestResult.second())) {
                            bestResult = Pair.of(child, result);
                        }
                        builder.markerRollbackTo(marker, null);
                    }*/
                }
                child = child.getNext();
            }
            //marker.drop();

/*
            if (bestResult != null) {
                ElementTypeRef element = bestResult.first();
                ParseResult result = element.getParser().parse(node, true, depth + 1, context);
                return stepOut(node, context, depth, result.getType(), result.getMatchedTokens());
            }
*/

            if (!optional) {
                //updateBuilderError(builder, this);
            }

        }
        return stepOut(node, context, depth, ParseResultType.NO_MATCH, 0);
    }

}