package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.constant.Constant;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.constant.Constant.array;

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

    UNKNOWN(
            "Unknown",
            null,
            null,
            "java.sql.Driver",
            AuthenticationType.values(),
            array(DatabaseUrlPattern.UNKNOWN));

    private String name;
    private Icon icon;
    private Icon largeIcon;
    AuthenticationType[] authTypes;
    private DatabaseUrlPattern[] urlPatterns;
    private String driverClassName;
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

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    public Icon getLargeIcon() {
        return largeIcon;
    }

    public DatabaseUrlPattern[] getUrlPatterns() {
        return urlPatterns;
    }

    public AuthenticationType[] getAuthTypes() {
        return authTypes;
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
        return DatabaseUrlPattern.UNKNOWN;
    }

    @NotNull
    public static DatabaseType get(String name) {
        if (StringUtil.isNotEmpty(name)) {
            for (DatabaseType databaseType : values()) {
                if (name.equalsIgnoreCase(databaseType.name)) return databaseType;
            }
        }
        return UNKNOWN;
    }

    public static DatabaseType resolve(String name) {
        name = name == null ? "" : name.toUpperCase();
        if (name.contains("ORACLE")) {
            return DatabaseType.ORACLE;
        } else if (name.contains("MYSQL")) {
            return DatabaseType.MYSQL;
        } else if (name.contains("POSTGRESQL") || name.contains("REDSHIFT")) {
            return DatabaseType.POSTGRES;
        } else if (name.contains("SQLITE")) {
            return DatabaseType.SQLITE;
        }
        return UNKNOWN;
    }


    public String getDriverClassName() {
        return driverClassName;
    }
}
