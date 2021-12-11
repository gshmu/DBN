package com.dci.intellij.dbn.language.common.element.parser.impl;

import com.dci.intellij.dbn.language.common.element.impl.BasicElementType;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class BasicElementTypeParser extends ElementTypeParser<BasicElementType> {
    public BasicElementTypeParser(BasicElementType elementType) {
        super(elementType);
    }

    @Override
    public ParseResult parse(ParsePathNode parentNode, ParserContext context) {
        return ParseResult.noMatch();
    }
}