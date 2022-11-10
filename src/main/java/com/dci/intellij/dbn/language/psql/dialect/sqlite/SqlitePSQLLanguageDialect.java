package com.dci.intellij.dbn.language.psql.dialect.sqlite;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.sqlite.SqliteSQLParserDefinition;

public class SqlitePSQLLanguageDialect extends PSQLLanguageDialect {

    public SqlitePSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.SQLITE_PSQL);
    }

    @Override
    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new SqlitePSQLHighlighter(this);
    }

    @Override
    protected SqliteSQLParserDefinition createParserDefinition() {
        SqlitePSQLParser parser = new SqlitePSQLParser(this);
        return new SqliteSQLParserDefinition(parser);
    }
}