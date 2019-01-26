package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCustomCodeStyleSettings;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.iso92.Iso92SQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.mysql.MysqlSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.oracle.OracleSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.postgres.PostgresSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.sqlite.SqliteSQLLanguageDialect;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.tree.IFileElementType;

public class SQLLanguage extends DBLanguage<SQLLanguageDialect> {
    public static final SQLLanguage INSTANCE = new SQLLanguage();

    @Override
    protected SQLLanguageDialect[] createLanguageDialects() {
        SQLLanguageDialect oracleSQLLanguageDialect = new OracleSQLLanguageDialect();
        SQLLanguageDialect mysqlSQLLanguageDialect = new MysqlSQLLanguageDialect();
        SQLLanguageDialect postgresSQLLanguageDialect = new PostgresSQLLanguageDialect();
        SQLLanguageDialect sqliteSQLLanguageDialect = new SqliteSQLLanguageDialect();
        SQLLanguageDialect iso92SQLLanguageDialect = new Iso92SQLLanguageDialect();
        return new SQLLanguageDialect[]{
                oracleSQLLanguageDialect,
                mysqlSQLLanguageDialect,
                postgresSQLLanguageDialect,
                sqliteSQLLanguageDialect,
                iso92SQLLanguageDialect};
    }

    @Override
    public SQLLanguageDialect getMainLanguageDialect() {
        return getAvailableLanguageDialects()[0];
    }

    @Override
    protected IFileElementType createFileElementType(DBLanguage<SQLLanguageDialect> language) {
        return new SQLFileElementType(this);
    }

    private SQLLanguage() {
        super("DBN-SQL", "text/sql");
    }


    @Override
    public SQLCodeStyleSettings getCodeStyleSettings(Project project) {
        CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager.getSettings(project);
        SQLCustomCodeStyleSettings customCodeStyleSettings = codeStyleSettings.getCustomSettings(SQLCustomCodeStyleSettings.class);
        return customCodeStyleSettings.getCodeStyleSettings();
    }
}
