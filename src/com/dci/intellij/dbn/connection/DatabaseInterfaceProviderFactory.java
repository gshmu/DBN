package com.dci.intellij.dbn.connection;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.generic.GenericInterfaceProvider;
import com.dci.intellij.dbn.database.mysql.MySqlInterfaceProvider;
import com.dci.intellij.dbn.database.oracle.OracleInterfaceProvider;
import com.dci.intellij.dbn.database.postgres.PostgresInterfaceProvider;

public class DatabaseInterfaceProviderFactory {
    // fixme replace with generic data dictionary
    public static final DatabaseInterfaceProvider GENERIC_INTERFACE_PROVIDER = new GenericInterfaceProvider();
    public static final DatabaseInterfaceProvider ORACLE_INTERFACE_PROVIDER = new OracleInterfaceProvider();
    public static final DatabaseInterfaceProvider MYSQL_INTERFACE_PROVIDER = new MySqlInterfaceProvider();
    public static final DatabaseInterfaceProvider POSTGRES_INTERFACE_PROVIDER = new PostgresInterfaceProvider();

    public static DatabaseInterfaceProvider getInterfaceProvider(@NotNull ConnectionHandler connectionHandler) throws SQLException {
        DatabaseType databaseType;
        if (connectionHandler.isVirtual()) {
            databaseType = connectionHandler.getDatabaseType();
        } else {
            ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
            databaseType = databaseSettings.getDatabaseType();
            if (databaseType == null || databaseType == DatabaseType.UNKNOWN) {
                try {
                    databaseType = ConnectionUtil.getDatabaseType(connectionHandler.getStandaloneConnection());
                    databaseSettings.setDatabaseType(databaseType);
                } catch (SQLException e) {
                    if (databaseSettings.getDatabaseType() == null) {
                        databaseSettings.setDatabaseType(DatabaseType.UNKNOWN);
                    }
                    throw e;
                }
            }
        }

        return get(databaseType);
    }

    @NotNull
    public static DatabaseInterfaceProvider get(DatabaseType databaseType) {
        switch (databaseType) {
            case ORACLE: return ORACLE_INTERFACE_PROVIDER;
            case MYSQL: return MYSQL_INTERFACE_PROVIDER;
            case POSTGRES: return POSTGRES_INTERFACE_PROVIDER;
            default: return GENERIC_INTERFACE_PROVIDER;
        }
    }

    public static void reset() {
        GENERIC_INTERFACE_PROVIDER.reset();
        ORACLE_INTERFACE_PROVIDER.reset();
        MYSQL_INTERFACE_PROVIDER.reset();
        POSTGRES_INTERFACE_PROVIDER.reset();
    }
}
