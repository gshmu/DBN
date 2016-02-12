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

public class SqliteInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private DatabaseMessageParserInterface MESSAGE_PARSER_INTERFACE = new SqliteMessageParserInterface();
    private DatabaseCompatibilityInterface COMPATIBILITY_INTERFACE = new SqliteCompatibilityInterface(this);
    private DatabaseMetadataInterface METADATA_INTERFACE = new SqliteMetadataInterface(this);
    private DatabaseDDLInterface DDL_INTERFACE = new SqliteDDLInterface(this);
    private DatabaseExecutionInterface EXECUTION_INTERFACE = new SqliteExecutionInterface();
    private DatabaseNativeDataTypes NATIVE_DATA_TYPES = new SqliteNativeDataTypes();

    public SqliteInterfaceProvider() {
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.SQLITE_SQL),
                PSQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.SQLITE_PSQL));
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }

    public DatabaseNativeDataTypes getNativeDataTypes() {
        return NATIVE_DATA_TYPES;
    }

    public DatabaseMessageParserInterface getMessageParserInterface() {
        return MESSAGE_PARSER_INTERFACE;
    }

    public DatabaseCompatibilityInterface getCompatibilityInterface() {
        return COMPATIBILITY_INTERFACE;
    }

    public DatabaseMetadataInterface getMetadataInterface() {
        return METADATA_INTERFACE;
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return null;
    }

    public DatabaseDDLInterface getDDLInterface() {
        return DDL_INTERFACE;
    }

    public DatabaseExecutionInterface getDatabaseExecutionInterface() {
        return EXECUTION_INTERFACE;
    }


}
