package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ExecVariableElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class ExecVariableElementTypeParser extends ElementTypeParser<ExecVariableElementType> {
    public ExecVariableElementTypeParser(ExecVariableElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) {
        ParserBuilder builder = context.getBuilder();
        TokenType tokenType = builder.getTokenType();
        if (tokenType != null && !tokenType.isChameleon()){
            if (tokenType.isVariable()) {
                PsiBuilder.Marker marker = builder.mark();
                builder.advanceLexer(parentNode);
                return stepOut(marker, null, context, ParseResultType.FULL_MATCH, 1);
            }
        }
        return stepOut(null, null, context, ParseResultType.NO_MATCH, 0);
    }

}
