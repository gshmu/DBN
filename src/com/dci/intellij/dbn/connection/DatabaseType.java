package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.StringUtil;

public enum DatabaseType implements Presentable {
    ORACLE   ("ORACLE",   "Oracle",     Icons.DB_ORACLE,     Icons.DB_ORACLE_LARGE,     DatabaseUrlResolver.ORACLE, "oracle.jdbc.driver.OracleDriver"),
    MYSQL    ("MYSQL",    "MySQL",      Icons.DB_MYSQL,      Icons.DB_MYSQL_LARGE,      DatabaseUrlResolver.MYSQL, "com.mysql.jdbc.Driver"),
    POSTGRES ("POSTGRES", "PostgreSQL", Icons.DB_POSTGRESQL, Icons.DB_POSTGRESQL_LARGE, DatabaseUrlResolver.POSTGRES, "org.postgresql.Driver"),
    UNKNOWN  ("UNKNOWN",  "Unknown", null, null, DatabaseUrlResolver.UNKNOWN, "");

    private String name;
    private String displayName;
    private Icon icon;
    private Icon largeIcon;
    private DatabaseUrlResolver urlResolver;
    private String driverClassName;


    DatabaseType(String name, String displayName, Icon icon, Icon largeIcon, DatabaseUrlResolver urlResolver, String driverClassName) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.urlResolver = urlResolver;
        this.driverClassName = driverClassName;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon getLargeIcon() {
        return largeIcon;
    }

    public DatabaseUrlResolver getUrlResolver() {
        return urlResolver;
    }

    public static DatabaseType get(String name) {
        if (StringUtil.isNotEmpty(name)) {
            for (DatabaseType databaseType : values()) {
                if (name.equalsIgnoreCase(databaseType.name)) return databaseType;
            }
        }
        return null;
    }

    public static DatabaseType resolve(String name) {
        name = name == null ? "" : name.toUpperCase();
        if (name.contains("ORACLE")) {
            return DatabaseType.ORACLE;
        } else if (name.contains("MYSQL")) {
            return DatabaseType.MYSQL;
        } else if (name.contains("POSTGRESQL")) {
            return DatabaseType.POSTGRES;
        }
        return UNKNOWN;
    }


    public String getDriverClassName() {
        return driverClassName;
    }
}
