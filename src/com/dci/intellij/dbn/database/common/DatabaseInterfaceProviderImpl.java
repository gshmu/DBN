package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public abstract class DatabaseInterfaceProviderImpl implements DatabaseInterfaceProvider {
    private static ThreadLocal<Project> PROJECT = new ThreadLocal<Project>();
    private static ThreadLocal<Cache> META_DATA_CACHE = new ThreadLocal<Cache>();

    private SQLLanguageDialect sqlLanguageDialect;
    private PSQLLanguageDialect psqlLanguageDialect;

    protected DatabaseInterfaceProviderImpl(SQLLanguageDialect sqlLanguageDialect, @Nullable PSQLLanguageDialect psqlLanguageDialect) {
        this.sqlLanguageDialect = sqlLanguageDialect;
        this.psqlLanguageDialect = psqlLanguageDialect;
    }

    @Override
    public Project getProject() {
        return PROJECT.get();
    }

    @Override
    public void setProject(Project project) {
        PROJECT.set(project);
    }

    @Override
    public void setMetaDataCache(Cache metaDataCache) {
        META_DATA_CACHE.set(metaDataCache);
    }

    public static Cache getMetaDataCache() {
        return META_DATA_CACHE.get();
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
        getDDLInterface().reset();
        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
        if (debuggerInterface != null) debuggerInterface.reset();
    }
}
