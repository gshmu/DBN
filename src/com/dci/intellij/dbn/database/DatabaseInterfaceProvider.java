package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import org.jetbrains.annotations.Nullable;

public interface DatabaseInterfaceProvider {

    DatabaseType getDatabaseType();

    @Nullable
    DBLanguageDialect getLanguageDialect(DBLanguage language);

    DatabaseNativeDataTypes getNativeDataTypes();

    DatabaseMessageParserInterface getMessageParserInterface();

    DatabaseCompatibilityInterface getCompatibilityInterface();

    DatabaseMetadataInterface getMetadataInterface();

    DatabaseDebuggerInterface getDebuggerInterface();

    DatabaseDDLInterface getDDLInterface();

    DatabaseExecutionInterface getDatabaseExecutionInterface();

    void reset();
}
