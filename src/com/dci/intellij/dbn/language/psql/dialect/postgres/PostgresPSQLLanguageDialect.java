package com.dci.intellij.dbn.language.psql.dialect.postgres;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;

public class PostgresPSQLLanguageDialect extends PSQLLanguageDialect {
    public PostgresPSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.POSTGRES_PSQL);
    }

    @Override
    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new PostgresPSQLHighlighter(this);
}

    @Override
    protected PostgresPSQLParserDefinition createParserDefinition() {
        PostgresPSQLParser parser = new PostgresPSQLParser(this);
        return new PostgresPSQLParserDefinition(parser);
    }

}
