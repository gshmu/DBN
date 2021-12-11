package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class TokenElementTypeParser extends ElementTypeParser<TokenElementType> {
    public TokenElementTypeParser(TokenElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) {
        ParserBuilder builder = context.getBuilder();

        TokenType token = builder.getTokenType();
        if (token == elementType.getTokenType() || isDummyToken(builder.getTokenText())) {

            String text = elementType.getText();
            if (Strings.isNotEmpty(text) && Strings.equalsIgnoreCase(builder.getTokenText(), text)) {
                PsiBuilder.Marker marker = builder.mark();
                builder.advanceLexer(parentNode);
                return stepOut(marker, null, context, ParseResultType.FULL_MATCH, 1);
            }

            SharedTokenTypeBundle sharedTokenTypes = getElementBundle().getTokenTypeBundle().getSharedTokenTypes();
            SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
            SimpleTokenType dot = sharedTokenTypes.getChrDot();

            if (token.isSuppressibleReservedWord()) {
                TokenType nextTokenType = builder.lookAhead(1);
                if (nextTokenType == dot && !elementType.isNextPossibleToken(dot, parentNode, context)) {
                    context.setWavedTokenType(token);
                    return stepOut(null, null, context, ParseResultType.NO_MATCH, 0);
                }
                if (token.isFunction() && elementType.getFlavor() == null) {
                    if (nextTokenType != leftParenthesis && elementType.isNextRequiredToken(leftParenthesis, parentNode, context)) {
                        context.setWavedTokenType(token);
                        return stepOut(null, null, context, ParseResultType.NO_MATCH, 0);
                    }
                }
            }

            PsiBuilder.Marker marker = builder.mark();
            builder.advanceLexer(parentNode);
            return stepOut(marker, null, context, ParseResultType.FULL_MATCH, 1);
        }
        return stepOut(null, null, context, ParseResultType.NO_MATCH, 0);
    }
}
