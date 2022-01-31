package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParserNode;
import com.intellij.lang.PsiBuilder.Marker;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class TokenPairMonitor {
    private final Map<TokenPairTemplate, TokenPairStack> stacks;
    private final ParserBuilder builder;

    private final boolean effective = Diagnostics.isAlternativeParserEnabled();

    public TokenPairMonitor(ParserBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;

        TokenPairTemplate[] tokenPairTemplates = languageDialect.getTokenPairTemplates();
        stacks = new EnumMap<>(TokenPairTemplate.class);
        for (TokenPairTemplate tokenPairTemplate : tokenPairTemplates) {
            stacks.put(tokenPairTemplate, new TokenPairStack(builder, languageDialect, tokenPairTemplate));
        }
    }

    public void consumeBeginTokens(@Nullable ParserNode node) {
        if (effective && node != null) {
            WrappingDefinition wrapping = node.getElement().getWrapping();
            if (wrapping != null) {
                TokenElementType beginElement = wrapping.getBeginElementType();
                TokenType beginToken = beginElement.getTokenType();
                while(builder.getToken() == beginToken) {
                    Marker marker = builder.mark();
                    acknowledgeCurrent();
                    builder.advanceInternally();
                    marker.done(beginElement);
                }
            }
        }
    }

    public void consumeEndTokens(@Nullable ParserNode node) {
        if (effective && node != null) {
            WrappingDefinition wrapping = node.getElement().getWrapping();
            if (wrapping != null) {
                TokenElementType endElement = wrapping.getEndElementType();
                TokenType endToken = endElement.getTokenType();
                while (builder.getToken() == endToken) {
                    Marker marker = builder.mark();
                    acknowledgeCurrent();
                    builder.advanceInternally();
                    marker.done(endElement);
                }
            }
        }
    }

    public void acknowledgeCurrent() {
        if (effective) {
            TokenType token = builder.getToken();
            TokenPairStack tokenPairStack = getStack(token);
            if (tokenPairStack != null) {
                tokenPairStack.acknowledge();
            }
        }
    }

    public void reset() {
        for (TokenPairStack tokenPairStack : stacks.values()) {
            tokenPairStack.reset();
        }
    }

    @Nullable
    private TokenPairStack getStack(@Nullable TokenType tokenType) {
        if (tokenType != null) {
            TokenPairTemplate template = tokenType.getTokenPairTemplate();
            if (template != null) {
                return stacks.get(template);
            }
        }
        return null;
    }
}
