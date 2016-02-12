package com.dci.intellij.dbn.language.sql.dialect.iso92;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.sql.SQLSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;

public class Iso92SQLHighlighter extends SQLSyntaxHighlighter {
    public Iso92SQLHighlighter(SQLLanguageDialect languageDialect) {
        super(languageDialect, "iso92_sql_highlighter_tokens.xml");
    }

    @NotNull
    protected Lexer createLexer() {
        FlexLexer flexLexer = new Iso92SQLHighlighterFlexLexer(getTokenTypes());
        return new LayeredLexer(new FlexAdapter(flexLexer));
    }
}
