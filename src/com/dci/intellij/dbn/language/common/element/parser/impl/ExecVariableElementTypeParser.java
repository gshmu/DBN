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
import org.jetbrains.annotations.NotNull;

public class ExecVariableElementTypeParser extends ElementTypeParser<ExecVariableElementType> {
    public ExecVariableElementTypeParser(ExecVariableElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) {
        ParserBuilder builder = context.builder;
        logBegin(builder, optional, depth);
        TokenType tokenType = builder.getTokenType();
        if (tokenType != null && !tokenType.isChameleon()){
            if (tokenType.isVariable()) {
                PsiBuilder.Marker marker = builder.mark(null);
                builder.advanceLexer(parentNode);
                return stepOut(marker, null, context, depth, ParseResultType.FULL_MATCH, 1);
            }
        }
        return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
    }

}
