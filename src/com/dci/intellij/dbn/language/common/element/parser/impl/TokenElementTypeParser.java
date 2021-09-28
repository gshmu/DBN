package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.common.util.StringUtil;
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
import org.jetbrains.annotations.NotNull;

public class TokenElementTypeParser extends ElementTypeParser<TokenElementType> {
    public TokenElementTypeParser(TokenElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) {
        ParserBuilder builder = context.builder;
        logBegin(builder, optional, depth);

        TokenType tokenType = builder.getTokenType();
        if (tokenType == elementType.tokenType || isDummyToken(builder.getTokenText())) {

            String text = elementType.getText();
            if (StringUtil.isNotEmpty(text) && StringUtil.equalsIgnoreCase(builder.getTokenText(), text)) {
                PsiBuilder.Marker marker = builder.mark(null);
                builder.advanceLexer(parentNode);
                return stepOut(marker, null, context, depth, ParseResultType.FULL_MATCH, 1);
            }

            SharedTokenTypeBundle sharedTokenTypes = getElementBundle().getTokenTypeBundle().getSharedTokenTypes();
            SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
            SimpleTokenType dot = sharedTokenTypes.getChrDot();

            if (tokenType.isSuppressibleReservedWord()) {
                TokenType nextTokenType = builder.lookAhead(1);
                if (nextTokenType == dot && !elementType.isNextPossibleToken(dot, parentNode, context)) {
                    context.setWavedTokenType(tokenType);
                    return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
                }
                if (tokenType.isFunction() && elementType.getFlavor() == null) {
                    if (nextTokenType != leftParenthesis && elementType.isNextRequiredToken(leftParenthesis, parentNode, context)) {
                        context.setWavedTokenType(tokenType);
                        return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
                    }
                }
            }

            PsiBuilder.Marker marker = builder.mark(null);
            builder.advanceLexer(parentNode);
            return stepOut(marker, null, context, depth, ParseResultType.FULL_MATCH, 1);
        }
        return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
    }
}
