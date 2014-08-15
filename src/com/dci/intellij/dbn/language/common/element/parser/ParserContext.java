package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.intellij.lang.PsiBuilder;

public class ParserContext {
    private long timestamp = System.currentTimeMillis();
    private ParserBuilder builder;
    private double dialectVersion;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect, double version) {
        this.builder = new ParserBuilder(builder, languageDialect);
        this.dialectVersion = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ParserBuilder getBuilder() {
        return builder;
    }

    public double getDialectVersion() {
        return dialectVersion;
    }
}
