package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder.Marker;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

@Deprecated
public class OldTokenPairMonitor{
    private final Map<TokenPairTemplate, OldTokenPairStack> stacks;
    private final ParserBuilder builder;

    private final boolean effective = !Diagnostics.isAlternativeParserEnabled();

    public OldTokenPairMonitor(ParserBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;

        TokenPairTemplate[] tokenPairTemplates = languageDialect.getTokenPairTemplates();
        stacks = new EnumMap<>(TokenPairTemplate.class);
        for (TokenPairTemplate tokenPairTemplate : tokenPairTemplates) {
            stacks.put(tokenPairTemplate, new OldTokenPairStack(builder, languageDialect, tokenPairTemplate));
        }
    }

    protected void consumeBeginTokens(@Nullable ParsePathNode node) {
        if (effective && node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType beginElement = wrapping.getBeginElementType();
                TokenType beginToken = beginElement.getTokenType();
                while(builder.getToken() == beginToken) {
                    Marker beginTokenMarker = builder.mark();
                    acknowledge(false);
                    builder.advanceInternally();
                    beginTokenMarker.done(beginElement);
                }
            }
        }
    }

    protected void consumeEndTokens(@Nullable ParsePathNode node) {
        if (effective && node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType endElement = wrapping.getEndElementType();
                TokenType endToken = endElement.getTokenType();
                while (builder.getToken() == endToken && !isExplicitRange(endToken)) {
                    Marker endTokenMarker = builder.mark();
                    acknowledge(false);
                    builder.advanceInternally();
                    endTokenMarker.done(endElement);
                }
            }
        }
    }

    protected void acknowledge(boolean explicit) {
        if (effective) {
            TokenType token = builder.getToken();
            OldTokenPairStack tokenPairStack = getStack(token);
            if (tokenPairStack != null) {
                tokenPairStack.acknowledge(explicit);
            }
        }
    }

    public void cleanup() {
        for (OldTokenPairStack tokenPairStack : stacks.values()) {
            tokenPairStack.cleanup(true);
        }

    }

    public void rollback() {
        for (OldTokenPairStack tokenPairStack : stacks.values()) {
            tokenPairStack.rollback();
        }
    }

    @Nullable
    private OldTokenPairStack getStack(TokenType tokenType) {
        if (tokenType != null) {
            TokenPairTemplate template = tokenType.getTokenPairTemplate();
            if (template != null) {
                return stacks.get(template);
            }
        }
        return null;
    }

    public boolean isExplicitRange(TokenType tokenType) {
        OldTokenPairStack stack = getStack(tokenType);
        return stack != null && stack.isExplicitRange();
    }

    public void setExplicitRange(TokenType tokenType, boolean value) {
        OldTokenPairStack stack = getStack(tokenType);
        if (stack != null) stack.setExplicitRange(value);
    }
}
