package com.dci.intellij.dbn.language.sql.dialect.postgres;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

import java.util.Set;

public class PostgresSQLLanguageDialect extends SQLLanguageDialect {

    public PostgresSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.POSTGRES_SQL);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {
        return null;
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new PostgresSQLSyntaxHighlighter(this);
}
    protected PostgresSQLParserDefinition createParserDefinition() {
        PostgresSQLParser parser = new PostgresSQLParser(this);
        return new PostgresSQLParserDefinition(parser);
    }

}