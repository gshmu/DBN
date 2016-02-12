package com.dci.intellij.dbn.language.sql.dialect.mysql;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.sql.SQLSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;

public class MysqlSQLHighlighter extends SQLSyntaxHighlighter {
    public MysqlSQLHighlighter(SQLLanguageDialect languageDialect) {
        super(languageDialect, "mysql_sql_highlighter_tokens.xml");
    }

    @NotNull
    protected Lexer createLexer() {
        FlexLexer flexLexer = new MysqlSQLHighlighterFlexLexer(getTokenTypes());
        return new LayeredLexer(new FlexAdapter(flexLexer));
    }
}