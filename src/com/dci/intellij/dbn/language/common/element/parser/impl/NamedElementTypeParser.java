package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import org.jetbrains.annotations.NotNull;

public class NamedElementTypeParser extends SequenceElementTypeParser<NamedElementType>{
    public NamedElementTypeParser(NamedElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.builder;
        if (isRecursive(parentNode, builder.getCurrentOffset(), 2)) {
            return ParseResult.noMatch();
        }
        return super.parse(parentNode, optional, depth, context);
    }

    protected boolean isRecursive(ParsePathNode parseNode, int builderOffset, int iterations){
        while (parseNode != null &&  iterations > 0) {
            if (parseNode.elementType == elementType &&
                    parseNode.getStartOffset() == builderOffset) {
                //return true;
                iterations--;
            }
            parseNode = parseNode.parent;
        }
        return iterations == 0;
        //return false;
    }
}