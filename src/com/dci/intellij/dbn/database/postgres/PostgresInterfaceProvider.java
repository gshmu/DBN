package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.DatabaseInterfaceProviderImpl;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import lombok.Getter;

@Getter
public class PostgresInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private final DatabaseMessageParserInterface messageParserInterface = new PostgresMessageParserInterface();
    private final DatabaseCompatibilityInterface compatibilityInterface = new PostgresCompatibilityInterface(this);
    private final DatabaseMetadataInterface metadataInterface = new PostgresMetadataInterface(this);
    private final DatabaseDDLInterface ddlInterface = new PostgresDDLInterface(this);
    private final DatabaseExecutionInterface executionInterface = new PostgresExecutionInterface();
    private final DatabaseNativeDataTypes nativeDataTypes = new PostgresNativeDataTypes();
    private final DatabaseDebuggerInterface debuggerInterface = null;

    public PostgresInterfaceProvider() {
        super(
            SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.POSTGRES_SQL),
            PSQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.POSTGRES_PSQL));
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRES;
    }
}
