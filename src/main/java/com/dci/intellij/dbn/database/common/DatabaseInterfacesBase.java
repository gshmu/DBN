package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.interfaces.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import org.jetbrains.annotations.Nullable;

public abstract class DatabaseInterfacesBase implements DatabaseInterfaces {
    private final SQLLanguageDialect sqlLanguageDialect;
    private final PSQLLanguageDialect psqlLanguageDialect;

    protected DatabaseInterfacesBase(SQLLanguageDialect sqlLanguageDialect, @Nullable PSQLLanguageDialect psqlLanguageDialect) {
        this.sqlLanguageDialect = sqlLanguageDialect;
        this.psqlLanguageDialect = psqlLanguageDialect;
    }

    @Nullable
    @Override
    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        if (language == SQLLanguage.INSTANCE) return sqlLanguageDialect;
        if (language == PSQLLanguage.INSTANCE) return psqlLanguageDialect;
        return null;
    }

    @Override
    public void reset() {
        getMetadataInterface().reset();
        getDataDefinitionInterface().reset();
        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
        if (debuggerInterface != null) debuggerInterface.reset();
    }
}
