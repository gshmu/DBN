package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.element.impl.BlockElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class BlockElementTypeParser extends SequenceElementTypeParser<BlockElementType>{
    public BlockElementTypeParser(BlockElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        PsiBuilder.Marker marker = builder.mark();
        ParseResult result = super.parse(parentNode, context);
        if (result.getType() == ParseResultType.NO_MATCH) {
            builder.markerDrop(marker);
        } else {
            builder.markerDone(marker, elementType);
        }
        return result.getType() == ParseResultType.NO_MATCH ?
                ParseResult.noMatch() :
                ParseResult.fullMatch(result.getMatchedTokens());
    }
}