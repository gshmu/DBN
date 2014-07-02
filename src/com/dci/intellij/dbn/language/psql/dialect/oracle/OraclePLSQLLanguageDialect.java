package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;

public class OraclePLSQLLanguageDialect extends PSQLLanguageDialect {
    public OraclePLSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.ORACLE_PLSQL);
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new OraclePLSQLSyntaxHighlighter(this);
}

    protected OraclePLSQLParserDefinition createParserDefinition() {
        OraclePLSQLParser parser = new OraclePLSQLParser(this);
        return new OraclePLSQLParserDefinition(parser);
    }
}
