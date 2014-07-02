package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.intellij.lang.PsiBuilder;

public class ParserContext {
    private long timestamp = System.currentTimeMillis();
    private ParserBuilder builder;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = new ParserBuilder(builder, languageDialect);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ParserBuilder getBuilder() {
        return builder;
    }
}
