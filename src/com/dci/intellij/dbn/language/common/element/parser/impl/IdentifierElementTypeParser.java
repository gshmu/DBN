package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;
import org.jetbrains.annotations.NotNull;

public class IdentifierElementTypeParser extends AbstractElementTypeParser<IdentifierElementType> {
    public IdentifierElementTypeParser(IdentifierElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        TokenType tokenType = builder.getTokenType();
        if (tokenType != null && !tokenType.isChameleon()){
            if (tokenType.isIdentifier()) {
                PsiBuilder.Marker marker = builder.mark(null);
                builder.advanceLexer(parentNode);
                return stepOut(marker, depth, ParseResultType.FULL_MATCH, 1, null, context);
            }
            else if (getElementType().isDefinition() || isSuppressibleReservedWord(tokenType, parentNode)) {
                PsiBuilder.Marker marker = builder.mark(null);
                builder.advanceLexer(parentNode);
                return stepOut(marker, depth, ParseResultType.FULL_MATCH, 1, null, context);
            }
        }
        return stepOut(null, depth, ParseResultType.NO_MATCH, 0, null, context);
    }  
}