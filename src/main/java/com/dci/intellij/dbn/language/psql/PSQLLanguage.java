package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.code.psql.style.PSQLCodeStyle;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.mysql.MysqlPSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.oracle.OraclePLSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.postgres.PostgresPSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.sqlite.SqlitePSQLLanguageDialect;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.Nullable;

public class PSQLLanguage extends DBLanguage<PSQLLanguageDialect> {
    public static final PSQLLanguage INSTANCE = new PSQLLanguage();

    @Override
    protected PSQLLanguageDialect[] createLanguageDialects() {
        PSQLLanguageDialect oraclePLSQLLanguageDialect = new OraclePLSQLLanguageDialect();
        PSQLLanguageDialect mysqlPSQLLanguageDialect = new MysqlPSQLLanguageDialect();
        PSQLLanguageDialect postgresPSQLLanguageDialect = new PostgresPSQLLanguageDialect();
        PSQLLanguageDialect sqlitePSQLLanguageDialect = new SqlitePSQLLanguageDialect();
        return new PSQLLanguageDialect[]{
                oraclePLSQLLanguageDialect,
                mysqlPSQLLanguageDialect,
                postgresPSQLLanguageDialect,
                sqlitePSQLLanguageDialect};
    }

    @Override
    public PSQLLanguageDialect getMainLanguageDialect() {
        return getAvailableLanguageDialects()[0];
    }

    @Override
    protected IFileElementType createFileElementType(DBLanguage<PSQLLanguageDialect> language) {
        return new PSQLFileElementType(this);
    }

    private PSQLLanguage() {
        super("DBN-PSQL", "text/plsql");
    }

    @Override
    public PSQLCodeStyleSettings codeStyleSettings(@Nullable Project project) {
        return PSQLCodeStyle.settings(project);
    }
}