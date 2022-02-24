package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.generic.GenericInterfaceProvider;
import com.dci.intellij.dbn.database.mysql.MySqlInterfaceProvider;
import com.dci.intellij.dbn.database.oracle.OracleInterfaceProvider;
import com.dci.intellij.dbn.database.postgres.PostgresInterfaceProvider;
import com.dci.intellij.dbn.database.sqlite.SqliteInterfaceProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DatabaseInterfaceProviderFactory {
    // fixme replace with generic data dictionary
    static final DatabaseInterfaceProvider GENERIC_INTERFACE_PROVIDER = new GenericInterfaceProvider();
    private static final DatabaseInterfaceProvider ORACLE_INTERFACE_PROVIDER = new OracleInterfaceProvider();
    private static final DatabaseInterfaceProvider MYSQL_INTERFACE_PROVIDER = new MySqlInterfaceProvider();
    private static final DatabaseInterfaceProvider POSTGRES_INTERFACE_PROVIDER = new PostgresInterfaceProvider();
    private static final DatabaseInterfaceProvider SQLITE_INTERFACE_PROVIDER = new SqliteInterfaceProvider();

    public static DatabaseInterfaceProvider getInterfaceProvider(@NotNull ConnectionHandler connection) throws SQLException {
        DatabaseType databaseType;
        if (connection.isVirtual()) {
            databaseType = connection.getDatabaseType();
        } else {
            ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
            databaseType = databaseSettings.getDatabaseType();
/*
            TODO use resolvedDatabaseType to guess better interface provider ??
            if (databaseType == DatabaseType.UNKNOWN) {
                DBNConnection testConnection = null;
                try {
                    testConnection = connection.getTestConnection();
                    databaseType = ResourceUtil.getDatabaseType(testConnection);
                    databaseSettings.setDatabaseType(databaseType);
                }  finally {
                    ResourceUtil.close(testConnection);
                }
            }
*/
        }

        return get(databaseType);
    }

    @NotNull
    public static DatabaseInterfaceProvider get(DatabaseType databaseType) {
        switch (databaseType) {
            case ORACLE: return ORACLE_INTERFACE_PROVIDER;
            case MYSQL: return MYSQL_INTERFACE_PROVIDER;
            case POSTGRES: return POSTGRES_INTERFACE_PROVIDER;
            case SQLITE: return SQLITE_INTERFACE_PROVIDER;
            default: return GENERIC_INTERFACE_PROVIDER;
        }
    }

    public static void reset() {
        GENERIC_INTERFACE_PROVIDER.reset();
        ORACLE_INTERFACE_PROVIDER.reset();
        MYSQL_INTERFACE_PROVIDER.reset();
        POSTGRES_INTERFACE_PROVIDER.reset();
        SQLITE_INTERFACE_PROVIDER.reset();
    }
}
