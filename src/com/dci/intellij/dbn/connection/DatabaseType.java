package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.constant.Constant;
import com.dci.intellij.dbn.common.constant.ConstantUtil;
import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.constant.Constant.array;

@Getter
public enum DatabaseType implements Constant<DatabaseType>, Presentable{
    ORACLE(
            "Oracle",
            Icons.DB_ORACLE,
            Icons.DB_ORACLE_LARGE,
            "oracle.jdbc.driver.OracleDriver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.ORACLE_SID, DatabaseUrlPattern.ORACLE_SERVICE)),

    MYSQL(
            "MySQL",
            Icons.DB_MYSQL,
            Icons.DB_MYSQL_LARGE,
            "com.mysql.cj.jdbc.Driver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.MYSQL)),

    POSTGRES(
            "PostgreSQL",
            Icons.DB_POSTGRESQL,
            Icons.DB_POSTGRESQL_LARGE,
            "org.postgresql.Driver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.POSTGRES)),

    SQLITE(
            "SQLite",
            Icons.DB_SQLITE,
            Icons.DB_SQLITE_LARGE,
            "org.sqlite.JDBC",
            array(AuthenticationType.NONE),
            array(DatabaseUrlPattern.SQLITE)),

    GENERIC(
            "Generic",
            Icons.DB_GENERIC,
            Icons.DB_GENERIC_LARGE,
            "java.sql.Driver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.GENERIC)),

    @Deprecated // used for fallback on existing configs TODO decommission after a few releases
    UNKNOWN(
            "Unknown",
            null,
            null,
            "java.sql.Driver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.GENERIC));

    private final String name;
    private final Icon icon;
    private final Icon largeIcon;
    private final AuthenticationType[] authTypes;
    private final DatabaseUrlPattern[] urlPatterns;
    private final String driverClassName;
    private String internalLibraryPath;


    DatabaseType(
            String name,
            Icon icon,
            Icon largeIcon,
            String driverClassName,
            AuthenticationType[] authTypes,
            DatabaseUrlPattern[] urlPatterns) {

        this.name = name;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.urlPatterns = urlPatterns;
        this.authTypes = authTypes;
        this.driverClassName = driverClassName;
    }

    public boolean hasUrlPattern(DatabaseUrlPattern pattern) {
        for (DatabaseUrlPattern urlPattern : urlPatterns) {
            if (urlPattern == pattern) {
                return true;
            }
        }
        return false;
    }

    public DatabaseUrlType[] getUrlTypes() {
        DatabaseUrlType[] urlTypes = new DatabaseUrlType[urlPatterns.length];
        for (int i = 0; i < urlPatterns.length; i++) {
            DatabaseUrlPattern urlPattern = urlPatterns[i];
            urlTypes[i] = urlPattern.getUrlType();
        }
        return urlTypes;
    }

    public DatabaseUrlPattern getDefaultUrlPattern() {
        return urlPatterns[0];
    }

    @NotNull
    public DatabaseUrlPattern resolveUrlPattern(String url) {
        for (DatabaseUrlPattern urlPattern : urlPatterns) {
            if (urlPattern.matches(url)) {
                return urlPattern;
            }
        }
        return DatabaseUrlPattern.GENERIC;
    }

    @NotNull
    public static DatabaseType get(String id) {
        return ConstantUtil.get(values(), id, GENERIC);
    }

    @NotNull
    public static DatabaseType resolve(String ... identifiers) {
        for (String identifier : identifiers) {
            DatabaseType databaseType = strongMatch(identifier);
            if (databaseType != GENERIC) {
                return databaseType;
            }
        }

        return GENERIC;
    }

    public static DatabaseType derive(String ... identifiers) {
        // TODO derived database registry
        return resolve(identifiers);
    }

    private static DatabaseType strongMatch(String identifier) {
        identifier = identifier == null ? "" : identifier.toUpperCase();
        if (identifier.contains("ORACLE") || identifier.contains("OJDBC")) {
            return DatabaseType.ORACLE;
        } else if (identifier.contains("MYSQL")) {
            return DatabaseType.MYSQL;
        } else if (identifier.contains("POSTGRESQL") || identifier.contains("REDSHIFT")) {
            return DatabaseType.POSTGRES;
        } else if (identifier.contains("SQLITE")) {
            return DatabaseType.SQLITE;
        }
        return GENERIC;
    }


}
