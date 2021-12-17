package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.intellij.util.containers.Stack;

public class TokenPairStack {
    private final ParserBuilder builder;
    private final SimpleTokenType beginToken;
    private final SimpleTokenType endToken;

    private final Stack<Integer> offsetStack = new Stack<>();


    public TokenPairStack(ParserBuilder builder, DBLanguageDialect languageDialect, TokenPairTemplate template) {
        this.builder = builder;

        TokenTypeBundle parserTokenTypes = languageDialect.getParserTokenTypes();
        beginToken = parserTokenTypes.getTokenType(template.getBeginToken());
        endToken = parserTokenTypes.getTokenType(template.getEndToken());
    }

    /**
     * cleanup all markers registered after the builder offset (remained dirty after a marker rollback)
     */
    public void reset() {
        int builderOffset = builder.getOffset();
        while (offsetStack.size() > 0) {
            Integer lastOffset = offsetStack.peek();
            if (lastOffset >= builderOffset) {
                offsetStack.pop();
            } else {
                break;
            }
        }
    }

    public void acknowledge() {
        TokenType tokenType = builder.getToken();
        if (tokenType == beginToken) {
            offsetStack.push(builder.getOffset());

        } else if (tokenType == endToken) {
            offsetStack.pop();
        }
    }
}
