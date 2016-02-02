package com.dci.intellij.dbn.language.sql.dialect.sqlite;

import com.dci.intellij.dbn.language.sql.SQLParser;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

public class SqliteSQLParser extends SQLParser {
    public SqliteSQLParser(SQLLanguageDialect languageDialect) {
        super(languageDialect, "sqlite_sql_parser_tokens.xml", "sqlite_sql_parser_elements.xml", "sql_block");
    }
}