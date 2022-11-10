package com.dci.intellij.dbn.language.psql.dialect.oracle;

import com.dci.intellij.dbn.language.psql.PSQLParser;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;

class OraclePLSQLParser extends PSQLParser {
    OraclePLSQLParser(PSQLLanguageDialect languageDialect) {
        super(languageDialect, "oracle_plsql_parser_tokens.xml", "oracle_plsql_parser_elements.xml", "plsql_block");
    }
}