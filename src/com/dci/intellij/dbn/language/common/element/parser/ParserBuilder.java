package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.psi.tree.IElementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ParserBuilder {
    private final PsiBuilder builder;
    private final TokenPairMonitor tokenPairMonitor;
    private Cache cache;

    public ParserBuilder(PsiBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;
        this.builder.setDebugMode(false);
        this.tokenPairMonitor = new TokenPairMonitor(this, languageDialect);
    }

    public TokenPairMonitor getTokenPairMonitor() {
        return tokenPairMonitor;
    }

    public Marker markAndAdvanceLexer(ParsePathNode node) {
        Marker marker = mark();
        advanceLexer(node);
        return marker;
    }

    public void advanceLexer(ParsePathNode node) {
        tokenPairMonitor.acknowledge(node,true);
        advanceLexer();
    }

    public void advanceLexer() {
        builder.advanceLexer();
        cache = null;
    }

    public boolean isDummyToken(){
        return getCache().isDummyToken();
    }

    public String getTokenText() {
        return getCache().getTokenText();
    }

    private Cache getCache() {
        if (cache == null) {
            String tokenText = builder.getTokenText();
            boolean dummyToken = tokenText != null && tokenText.contains(CodeCompletionContributor.DUMMY_TOKEN);
            cache = new Cache(tokenText, dummyToken);
        }
        return cache;
    }

    @Nullable
    public TokenType getTokenType() {
        IElementType tokenType = builder.getTokenType();
        return tokenType instanceof TokenType ? (TokenType) tokenType : null;
    }

    public boolean eof() {
        return builder.eof();
    }

    public int getCurrentOffset() {
        return builder.getCurrentOffset();
    }

    @Nullable
    public TokenType lookAhead(int steps) {
        IElementType elementType = builder.lookAhead(steps);
        return elementType instanceof TokenType ? (TokenType) elementType : null;
    }


    public TokenType lookBack(int steps) {
        int cursor = -1;
        int count = 0;
        TokenType tokenType = (TokenType) builder.rawLookup(cursor);
        while (tokenType != null && count <= steps) {
            TokenTypeCategory category = tokenType.getCategory();
            if (category != TokenTypeCategory.WHITESPACE && category != TokenTypeCategory.COMMENT) {
                count++;
                if (count == steps) return tokenType;
            }
            cursor--;
            tokenType = (TokenType) builder.rawLookup(cursor);
        }
        return null;
    }

    public IElementType rawLookup(int steps) {
        return builder.rawLookup(steps);
    }


    public void error(String messageText) {
        builder.error(messageText);
    }

    public ASTNode getTreeBuilt() {
        tokenPairMonitor.cleanup();
        return builder.getTreeBuilt();
    }

    public Marker mark(){
        return builder.mark();
    }

    public Marker mark(ParsePathNode node){
        tokenPairMonitor.consumeBeginTokens(node);
        return builder.mark();
    }


    public void markError(String message) {
        Marker errorMaker = builder.mark();
        errorMaker.error(message);
    }

    public void markerRollbackTo(Marker marker) {
        if (marker != null) {
            marker.rollbackTo();
            tokenPairMonitor.rollback();
            cache = null;;
        }
    }

    public void markerDone(Marker marker, ElementType elementType) {
        markerDone(marker, elementType, null);
    }

    public void markerDone(Marker marker, ElementType elementType, @Nullable ParsePathNode node) {
        if (marker != null) {
            tokenPairMonitor.consumeEndTokens(node);
            marker.done((IElementType) elementType);
        }
    }

    public void markerDrop(Marker marker) {
        if (marker != null) {
            marker.drop();
        }
    }

    @Getter
    @AllArgsConstructor
    private static class Cache {
        private final String tokenText;
        private final boolean dummyToken;
    }
}
