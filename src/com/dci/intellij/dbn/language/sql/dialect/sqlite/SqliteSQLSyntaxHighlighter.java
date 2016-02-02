package com.dci.intellij.dbn.language.sql.dialect.sqlite;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.sql.SQLSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;

public class SqliteSQLSyntaxHighlighter extends SQLSyntaxHighlighter {
    public SqliteSQLSyntaxHighlighter(SQLLanguageDialect languageDialect) {
        super(languageDialect, "sqlite_sql_highlighter_tokens.xml");
    }

    @NotNull
    protected Lexer createLexer() {
        FlexLexer flexLexer = new SqliteSQLHighlighterFlexLexer(getTokenTypes());
        return new LayeredLexer(new FlexAdapter(flexLexer));
    }
}