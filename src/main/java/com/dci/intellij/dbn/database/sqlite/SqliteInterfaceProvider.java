package com.dci.intellij.dbn.database.sqlite;

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
public class SqliteInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private final DatabaseMessageParserInterface messageParserInterface = new SqliteMessageParserInterface();
    private final DatabaseCompatibilityInterface compatibilityInterface = new SqliteCompatibilityInterface(this);
    private final DatabaseMetadataInterface metadataInterface = new SqliteMetadataInterface(this);
    private final DatabaseDDLInterface ddlInterface = new SqliteDDLInterface(this);
    private final DatabaseExecutionInterface executionInterface = new SqliteExecutionInterface();
    private final DatabaseNativeDataTypes nativeDataTypes = new SqliteNativeDataTypes();
    private final DatabaseDebuggerInterface debuggerInterface = null;

    public SqliteInterfaceProvider() {
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.SQLITE_SQL),
                PSQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.SQLITE_PSQL));
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }
}
