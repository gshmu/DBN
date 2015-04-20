package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.StringUtil;

public enum DatabaseType implements Presentable {
    ORACLE   ("ORACLE",   "Oracle",     Icons.DB_ORACLE,     Icons.DB_ORACLE_LARGE,     "jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>"),
    MYSQL    ("MYSQL",    "MySQL",      Icons.DB_MYSQL,      Icons.DB_MYSQL_LARGE,      "jdbc:mysql://[HOST]:[PORT]/[DATABASE]"),
    POSTGRES ("POSTGRES", "PostgreSQL", Icons.DB_POSTGRESQL, Icons.DB_POSTGRESQL_LARGE, "jdbc:postgresql://<HOST>:<PORT>/<DATABASE>"),
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

    public String getUrlPattern() {
        return urlPattern;
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
}
