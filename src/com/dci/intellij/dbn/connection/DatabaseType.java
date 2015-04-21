package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.StringUtil;

public enum DatabaseType implements Presentable {
    ORACLE   ("ORACLE",   "Oracle",     Icons.DB_ORACLE,     Icons.DB_ORACLE_LARGE,     "jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>", "^(jdbc:oracle:thin:@)([.a-z0-9]+)(:[0-9]+)?:([a-z0.9]+)$"),
    MYSQL    ("MYSQL",    "MySQL",      Icons.DB_MYSQL,      Icons.DB_MYSQL_LARGE,      "jdbc:mysql://[HOST]:[PORT]/[DATABASE]",      "^(jdbc:mysql:\\/\\/)([.a-z0-9]+)(:[0-9]+)?(\\/[a-z0-9]+)?$"),
    POSTGRES ("POSTGRES", "PostgreSQL", Icons.DB_POSTGRESQL, Icons.DB_POSTGRESQL_LARGE, "jdbc:postgresql://<HOST>:<PORT>/<DATABASE>", "^(jdbc:postgresql:\\/\\/)([.a-z0-9]+)(:[0-9]+)?\\/([a-z0-9]+)?$"),
    UNKNOWN  ("UNKNOWN",  "Unknown", null, null, "", "");

    private String name;
    private String displayName;
    private Icon icon;
    private Icon largeIcon;
    private String urlPattern;
    private String urlRegex;


    DatabaseType(String name, String displayName, Icon icon, Icon largeIcon, String urlPattern, String urlRegex) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.urlPattern = urlPattern;
        this.urlRegex = urlRegex;
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

    public String getUrlRegex() {
        return urlRegex;
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

    public String resolveHost(String url) {
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return "";
    }

    public String resolvePort(String url) {
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            String portGroup = matcher.group(3);
            if (StringUtil.isNotEmpty(portGroup)) {
                return portGroup.substring(1);
            }
        }
        return "";
    }

    public String resolveDatabase(String url) {
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            String databaseGroup = matcher.group(4);
            if (StringUtil.isNotEmpty(databaseGroup)) {
                return databaseGroup.substring(1);
            }
        }
        return "";
    }

}
