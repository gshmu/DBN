package com.dci.intellij.dbn.language.psql.dialect.mysql;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.oracle.OraclePLSQLHighlighter;

public class MysqlPSQLLanguageDialect extends PSQLLanguageDialect {
    public MysqlPSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.MYSQL_PSQL);
    }

    @Override
    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new OraclePLSQLHighlighter(this);
}

    @Override
    protected MysqlPSQLParserDefinition createParserDefinition() {
        MysqlPSQLParser parser = new MysqlPSQLParser(this);
        return new MysqlPSQLParserDefinition(parser);
    }
}
