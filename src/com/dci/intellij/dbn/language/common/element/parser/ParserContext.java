package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.cache.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.intellij.lang.PsiBuilder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParserContext extends ElementLookupContext {
    private final long timestamp = System.currentTimeMillis();
    private final ParserBuilder builder;
    private transient LeafElementType lastResolvedLeaf;
    private TokenType wavedTokenType;
    private int wavedTokenTypeOffset;
    private boolean alternative = Diagnostics.isAlternativeParserEnabled();

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
