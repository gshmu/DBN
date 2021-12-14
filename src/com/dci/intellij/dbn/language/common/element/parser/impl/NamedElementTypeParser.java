package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class NamedElementTypeParser extends SequenceElementTypeParser<NamedElementType>{
    public NamedElementTypeParser(NamedElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        if (isRecursive(parentNode, builder.getCurrentOffset())) {
            return ParseResult.noMatch();
        }
        return super.parse(parentNode, context);
    }

    protected boolean isRecursive(ParsePathNode parseNode, int builderOffset){
        // allow 2 levels of recursivity
        boolean recursive = false;
        while (parseNode != null) {
            if (parseNode.getElementType() == elementType &&
                    parseNode.getStartOffset() == builderOffset) {
                if (recursive) {
                    return true;
                } else {
                    recursive = true;
                }
            }
            parseNode = parseNode.getParent();
        }
        return false;
    }
}