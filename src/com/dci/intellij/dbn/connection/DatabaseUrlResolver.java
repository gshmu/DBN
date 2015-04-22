package com.dci.intellij.dbn.connection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

public enum DatabaseUrlResolver {

    ORACLE  ("jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>",  "^(jdbc:oracle:thin:@)([._a-z0-9]+)(:[0-9]+)(:[a-z0-9]+)$",         "localhost", "1521", "XE"),
    MYSQL   ("jdbc:mysql://<HOST>:<PORT>/<DATABASE>",       "^(jdbc:mysql:\\/\\/)([._a-z0-9]+)(:[0-9]+)?(\\/[a-z0-9]+)?$",      "localhost", "3306", "mysql"),
    POSTGRES("jdbc:postgresql://<HOST>:<PORT>/<DATABASE>",  "^(jdbc:postgresql:\\/\\/)([._a-z0-9]+)(:[0-9]+)?(\\/[a-z0-9]*)?$", "localhost", "5432", "postgres"),
    UNKNOWN ("",  "", "localhost", "1234", "database"),
    ;

    private String urlPattern;
    private String urlRegex;
    private String defaultHost;
    private String defaultPort;
    private String defaultDatabase;

    public String getUrl(String host, String port, String database) {
        return urlPattern.
                replace("<HOST>", CommonUtil.nvl(host, "")).
                replace("<PORT>", CommonUtil.nvl(port, "")).
                replace("<DATABASE>", CommonUtil.nvl(database, ""));
    }

    public String getDefaultUrl() {
        return getUrl(defaultHost, defaultPort, defaultDatabase);
    }

    DatabaseUrlResolver(String urlPattern, String urlRegex, String defaultHost, String defaultPort, String defaultDatabase) {
        this.urlPattern = urlPattern;
        this.urlRegex = urlRegex;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
        this.defaultDatabase = defaultDatabase;
    }

    public String resolveHost(String url) {
        if (StringUtil.isNotEmpty(url)) {
            Matcher matcher = getMatcher(url);
            if (matcher.matches()) {
                return matcher.group(2);
            }
        }
        return "";
    }

    public String resolvePort(String url) {
        if (StringUtil.isNotEmpty(url)) {
            Matcher matcher = getMatcher(url);
            if (matcher.matches()) {
                String portGroup = matcher.group(3);
                if (StringUtil.isNotEmpty(portGroup)) {
                    return portGroup.substring(1);
                }
            }
        }
        return "";
    }

    public String resolveDatabase(String url) {
        if (StringUtil.isNotEmpty(url)) {
            Matcher matcher = getMatcher(url);
            if (matcher.matches()) {
                String databaseGroup = matcher.group(4);
                if (StringUtil.isNotEmpty(databaseGroup)) {
                    return databaseGroup.substring(1);
                }
            }
        }
        return "";
    }

    public boolean isValid(String url) {
        if (StringUtil.isNotEmpty(url)) {
            Matcher matcher = getMatcher(url);
            return matcher.matches();
        }
        return false;
    }

    @NotNull
    private Matcher getMatcher(String url) {
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(url);
    }
}
