package com.dci.intellij.dbn.language.common.element.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

public class ParserBuilder {
    private PsiBuilder builder;
    private NestedRangeMonitor nestingMonitor;


    public ParserBuilder(PsiBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;
        builder.setDebugMode(true);
        this.nestingMonitor = new NestedRangeMonitor(builder, languageDialect);
    }

    public void advanceLexer(@NotNull ParsePathNode node) {
        advanceLexer(node, false);
    }

    public void advanceLexer(@NotNull ParsePathNode node, boolean mark) {
        nestingMonitor.compute(node, mark);
        builder.advanceLexer();
    }


    public String getTokenText() {
        return builder.getTokenText();
    }

    public TokenType getTokenType() {
        return (TokenType) builder.getTokenType();
    }

    public boolean eof() {
        return builder.eof();
    }

    public int getCurrentOffset() {
        return builder.getCurrentOffset();
    }

    public TokenType lookAhead(int steps) {
        return (TokenType) builder.lookAhead(steps);
    }

    public void error(String messageText) {
        builder.error(messageText);
    }

    public ASTNode getTreeBuilt() {
        nestingMonitor.cleanup(true);
        return builder.getTreeBuilt();
    }

    public void setDebugMode(boolean debugMode) {
        builder.setDebugMode(debugMode);
    }

    public PsiBuilder.Marker mark(@Nullable ParsePathNode node){
        PsiBuilder.Marker marker = builder.mark();
        if (node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType beginElementType = wrapping.getBeginElementType();
                TokenType beginTokenType = beginElementType.getTokenType();
                while(builder.getTokenType() == beginTokenType) {
                    PsiBuilder.Marker beginTokenMarker = builder.mark();
                    advanceLexer(node, true);
                    beginTokenMarker.done((IElementType) beginElementType);
                }
            }
        }
        return marker;
    }

    public void markerRollbackTo(PsiBuilder.Marker marker, @Nullable ParsePathNode node) {
        if (marker != null) {
            marker.rollbackTo();
            nestingMonitor.rollback();
        }
    }

    public void markerDone(PsiBuilder.Marker marker, ElementType elementType) {
        markerDone(marker, elementType, null);
    }

    public void markerDone(PsiBuilder.Marker marker, ElementType elementType, @Nullable ParsePathNode node) {
        if (marker != null) {
            if (node != null) {
                WrappingDefinition wrapping = node.getElementType().getWrapping();
                if (wrapping != null) {
                    TokenElementType endElementType = wrapping.getEndElementType();
                    TokenType endTokenType = endElementType.getTokenType();
                    while (builder.getTokenType() == endTokenType) {
                        PsiBuilder.Marker endTokenMarker = builder.mark();
                        advanceLexer(node, true);
                        endTokenMarker.done((IElementType) endElementType);
                    }
                }
            }
            marker.done((IElementType) elementType);
        }
    }

    public void markerDrop(PsiBuilder.Marker marker) {
        if (marker != null) {
            marker.drop();
        }
    }
}
