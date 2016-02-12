package com.dci.intellij.dbn.language.sql.dialect.sqlite;

import java.util.Set;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

public class SqliteSQLLanguageDialect extends SQLLanguageDialect {

    public SqliteSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.SQLITE_SQL);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {
        return null;
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new SqliteSQLHighlighter(this);
}
    protected SqliteSQLParserDefinition createParserDefinition() {
        SqliteSQLParser parser = new SqliteSQLParser(this);
        return new SqliteSQLParserDefinition(parser);
    }

}