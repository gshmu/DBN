package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public enum DatabaseType implements Presentable {
    ORACLE   ("ORACLE",   "Oracle",     Icons.DB_ORACLE,     Icons.DB_ORACLE_LARGE,     "oracle.jdbc.driver.OracleDriver", true, DatabaseUrlPattern.ORACLE_SID, DatabaseUrlPattern.ORACLE_SERVICE),
    MYSQL    ("MYSQL",    "MySQL",      Icons.DB_MYSQL,      Icons.DB_MYSQL_LARGE,      "com.mysql.jdbc.Driver", true, DatabaseUrlPattern.MYSQL),
    POSTGRES ("POSTGRES", "PostgreSQL", Icons.DB_POSTGRESQL, Icons.DB_POSTGRESQL_LARGE, "org.postgresql.Driver", true, DatabaseUrlPattern.POSTGRES),
    SQLITE   ("SQLITE",   "SQLite",     Icons.DB_SQLITE,     Icons.DB_SQLITE_LARGE,     "org.sqlite.JDBC",       false, DatabaseUrlPattern.SQLITE),
    UNKNOWN  ("UNKNOWN",  "Unknown",    null,                null,                      "java.sql.Driver",       true, DatabaseUrlPattern.UNKNOWN);

    private String name;
    private String displayName;
    private Icon icon;
    private Icon largeIcon;
    private DatabaseUrlPattern[] urlPatterns;
    private String driverClassName;
    private String internalLibraryPath;
    private boolean authenticationSupported;


    DatabaseType(String name, String displayName, Icon icon, Icon largeIcon, String driverClassName, boolean authenticationSupported, DatabaseUrlPattern... urlPatterns) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.urlPatterns = urlPatterns;
        this.driverClassName = driverClassName;
        this.authenticationSupported = authenticationSupported;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }



    @Override
    public Icon getIcon() {
        return icon;
    }

    public Icon getLargeIcon() {
        return largeIcon;
    }

    public boolean isAuthenticationSupported() {
        return authenticationSupported;
    }

    public DatabaseUrlPattern[] getUrlPatterns() {
        return urlPatterns;
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
