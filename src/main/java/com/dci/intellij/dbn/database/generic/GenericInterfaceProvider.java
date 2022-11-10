package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.database.*;
import com.dci.intellij.dbn.database.common.DatabaseInterfaceProviderImpl;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import lombok.Getter;

@Getter
public final class GenericInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private final DatabaseMessageParserInterface messageParserInterface = new GenericMessageParserInterface();
    private final DatabaseCompatibilityInterface compatibilityInterface = new GenericCompatibilityInterface(this);
    private final DatabaseMetadataInterface metadataInterface = new GenericMetadataInterface(this);
    private final DatabaseDDLInterface ddlInterface = new GenericDDLInterface(this);
    private final DatabaseExecutionInterface executionInterface = new GenericExecutionInterface();
    private final DatabaseNativeDataTypes nativeDataTypes = new GenericNativeDataTypes();
    private final DatabaseDebuggerInterface debuggerInterface = null;

    public GenericInterfaceProvider() {
        //super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.ISO92_SQL), null);
        // TODO ISO92 far from complete - fallback to SQLITE
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.SQLITE_SQL), null);
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.GENERIC;
    }
}
