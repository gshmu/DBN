package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.intellij.lang.PsiBuilder;

public class ParserContext extends ElementLookupContext {
    public final long timestamp = System.currentTimeMillis();
    public final ParserBuilder builder;
    public transient LeafElementType lastResolvedLeaf;
    private TokenType wavedTokenType;
    private int wavedTokenTypeOffset;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect, double databaseVersion) {
        super(null, databaseVersion);
        this.builder = new ParserBuilder(builder, languageDialect);
    }

    public boolean isWavedTokenType(TokenType tokenType) {
        return tokenType == wavedTokenType && builder.getCurrentOffset() == wavedTokenTypeOffset;
    }

    public void setWavedTokenType(TokenType wavedTokenType) {
        this.wavedTokenType = wavedTokenType;
        this.wavedTokenTypeOffset = builder.getCurrentOffset();
    }
}
