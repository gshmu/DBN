package com.dci.intellij.dbn.connection;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.Icons;

public enum DatabaseType {
    ORACLE   ("ORACLE",   "Oracle",     Icons.DB_ORACLE,     Icons.DB_ORACLE_LARGE,     "jdbc:oracle:thin:@[HOST]:[PORT]:[DATABASE]"),
    MYSQL    ("MYSQL",    "MySQL",      Icons.DB_MYSQL,      Icons.DB_MYSQL_LARGE,      "jdbc:mysql://[HOST]:[PORT]/[DATABASE]"),
    POSTGRES ("POSTGRES", "PostgreSQL", Icons.DB_POSTGRESQL, Icons.DB_POSTGRESQL_LARGE, "jdbc:postgresql://[HOST]:[PORT]/[DATABASE]"),
    UNKNOWN  ("UNKNOWN",  "Unknown", null, null, "");

    private String name;
    private String displayName;
    private Icon icon;
    private Icon largeIcon;
    private String urlPattern;


    DatabaseType(String name, String displayName, Icon icon, Icon largeIcon, String urlPattern) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.urlPattern = urlPattern;
    }

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

    public String getUrlPattern() {
        return urlPattern;
    }

    public static DatabaseType get(String name) {
        for (DatabaseType databaseType : values()) {
            if (name.equalsIgnoreCase(databaseType.name)) return databaseType;
        }
        return null;
    }
}
