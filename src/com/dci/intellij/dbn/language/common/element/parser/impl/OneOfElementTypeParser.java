package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;
import org.jetbrains.annotations.NotNull;

public class OneOfElementTypeParser extends AbstractElementTypeParser<OneOfElementType> {
    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = createParseNode(parentNode, builder.getCurrentOffset());
        PsiBuilder.Marker marker = builder.mark(node);

        getElementType().sort();
        TokenType tokenType = builder.getTokenType();

        if (tokenType!= null && !tokenType.isChameleon()) {
            String tokenText = builder.getTokenText();
            // TODO !!!! if elementType is an identifier: then BUILD VARIANTS!!!
            for (ElementType elementType : getElementType().getPossibleElementTypes()) {
                if (isDummyToken(tokenText) || elementType.getLookupCache().canStartWithToken(tokenType) || isSuppressibleReservedWord(tokenType, node)) {
                    ParseResult result = elementType.getParser().parse(node, true, depth + 1, context);
                    if (result.isMatch()) {
                        return stepOut(marker, depth, result.getType(), result.getMatchedTokens(), node, context);
                    }
                }
            }
            if (!optional) {
                //updateBuilderError(builder, this);
            }

        }
        return stepOut(marker, depth, ParseResultType.NO_MATCH, 0, node, context);
    }
}