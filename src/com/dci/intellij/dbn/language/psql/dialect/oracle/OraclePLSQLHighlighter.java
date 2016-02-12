package com.dci.intellij.dbn.language.psql.dialect.oracle;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.psql.PSQLSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;

public class OraclePLSQLHighlighter extends PSQLSyntaxHighlighter {
    public OraclePLSQLHighlighter(PSQLLanguageDialect languageDialect) {
        super(languageDialect, "oracle_plsql_highlighter_tokens.xml");
    }

    @NotNull
    protected Lexer createLexer() {
        FlexLexer flexLexer = new OraclePLSQLHighlighterFlexLexer(getTokenTypes());
        return new LayeredLexer(new FlexAdapter(flexLexer));
    }
}