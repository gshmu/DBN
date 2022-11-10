package com.dci.intellij.dbn.database.mysql;

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
public final class MySqlInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private final DatabaseMessageParserInterface messageParserInterface = new MySqlMessageParserInterface();
    private final DatabaseCompatibilityInterface compatibilityInterface = new MySqlCompatibilityInterface(this);
    private final DatabaseMetadataInterface metadataInterface = new MySqlMetadataInterface(this);
    private final DatabaseDDLInterface ddlInterface = new MySqlDDLInterface(this);
    private final DatabaseExecutionInterface executionInterface = new MySqlExecutionInterface();
    private final DatabaseNativeDataTypes nativeDataTypes = new MySqlNativeDataTypes();
    private final DatabaseDebuggerInterface debuggerInterface = null;

    public MySqlInterfaceProvider() {
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.MYSQL_SQL),
                PSQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.MYSQL_PSQL));
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}
