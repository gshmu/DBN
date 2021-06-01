package com.dci.intellij.dbn.database.oracle;

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
public class OracleInterfaceProvider extends DatabaseInterfaceProviderImpl {
    private final DatabaseMessageParserInterface messageParserInterface = new OracleMessageParserInterface();
    private final DatabaseCompatibilityInterface compatibilityInterface = new OracleCompatibilityInterface(this);
    private final DatabaseMetadataInterface metadataInterface = new OracleMetadataInterface(this);
    private final DatabaseDebuggerInterface debuggerInterface = new OracleDebuggerInterface(this);
    private final DatabaseDDLInterface ddlInterface = new OracleDDLInterface(this);
    private final DatabaseExecutionInterface executionInterface = new OracleExecutionInterface();
    private final DatabaseNativeDataTypes nativeDataTypes = new OracleNativeDataTypes();


    public OracleInterfaceProvider() {
        super(SQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.ORACLE_SQL),
                PSQLLanguage.INSTANCE.getLanguageDialect(DBLanguageDialectIdentifier.ORACLE_PLSQL));
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.ORACLE;
    }
}