package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder.Marker;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TokenPairMonitor {
    private final Map<TokenPairTemplate, TokenPairStack> stacks;
    private final ParserBuilder builder;

    public TokenPairMonitor(ParserBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;

        TokenPairTemplate[] tokenPairTemplates = languageDialect.getTokenPairTemplates();
        stacks = new HashMap<>(tokenPairTemplates.length);
        for (TokenPairTemplate tokenPairTemplate : tokenPairTemplates) {
            stacks.put(tokenPairTemplate, new TokenPairStack(builder, languageDialect, tokenPairTemplate));
        }
    }

    protected void consumeBeginTokens(@Nullable ParsePathNode node) {
        if (node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType beginElement = wrapping.getBeginElementType();
                TokenType beginToken = beginElement.getTokenType();
                while(builder.getTokenType() == beginToken) {
                    Marker beginTokenMarker = builder.mark();
                    acknowledge(node, false);
                    builder.advanceLexer();
                    beginTokenMarker.done(beginElement);
                }
            }
        }
    }

    protected void consumeEndTokens(@Nullable ParsePathNode node) {
        if (node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType endElement = wrapping.getEndElementType();
                TokenType endToken = endElement.getTokenType();
                while (builder.getTokenType() == endToken && !isExplicitRange(endToken)) {
                    Marker endTokenMarker = builder.mark();
                    acknowledge(node, false);
                    builder.advanceLexer();
                    endTokenMarker.done(endElement);
                }
            }
        }
    }

    protected void acknowledge(ParsePathNode node, boolean explicit) {
        TokenType token = builder.getTokenType();
        TokenPairStack tokenPairStack = getStack(token);
        if (tokenPairStack != null) {
            tokenPairStack.acknowledge(node, explicit);
        }
    }

    public void cleanup() {
        for (TokenPairStack tokenPairStack : stacks.values()) {
            tokenPairStack.cleanup(true);
        }

    }

    public void rollback() {
        for (TokenPairStack tokenPairStack : stacks.values()) {
            tokenPairStack.rollback();
        }
    }

    @Nullable
    protected TokenPairStack getStack(TokenType tokenType) {
        if (tokenType != null) {
            TokenPairTemplate template = tokenType.getTokenPairTemplate();
            if (template != null) {
                return stacks.get(template);
            }
        }
        return null;
    }

    public boolean isExplicitRange(TokenType tokenType) {
        TokenPairStack stack = getStack(tokenType);
        return stack != null && stack.isExplicitRange();
    }

    public void setExplicitRange(TokenType tokenType, boolean value) {
        TokenPairStack stack = getStack(tokenType);
        if (stack != null) stack.setExplicitRange(value);
    }
}
