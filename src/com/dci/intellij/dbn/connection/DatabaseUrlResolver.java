package com.dci.intellij.dbn.connection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

public enum DatabaseUrlResolver {

    ORACLE  ("jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>",  "^(jdbc:oracle:(?:thin|oci):@)([._\\-a-z0-9]+)(:[0-9]+)(:[$_a-z0-9]+)$",   DatabaseInfo.ORACLE),
    MYSQL   ("jdbc:mysql://<HOST>:<PORT>/<DATABASE>",       "^(jdbc:mysql:\\/\\/)([._\\-a-z0-9]+)(:[0-9]+)?(\\/[\\$_a-z0-9]*)?$",      DatabaseInfo.MYSQL),
    POSTGRES("jdbc:postgresql://<HOST>:<PORT>/<DATABASE>",  "^(jdbc:postgresql:\\/\\/)([._\\-a-z0-9]+)(:[0-9]+)?(\\/[\\$_a-z0-9]*)?$", DatabaseInfo.POSTGRES),
    UNKNOWN ("jdbc:unknown://<HOST>:<PORT>/<DATABASE>",     "^(jdbc:unknown:\\/\\/)([._\\-a-z0-9]+)(:[0-9]+)?(\\/[\\$_a-z0-9]*)?$",    DatabaseInfo.UNKNOWN),
    ;

    private String urlPattern;
    private String urlRegex;
    private DatabaseInfo defaultInfo;

    public String getUrl(DatabaseInfo databaseInfo) {
        return getUrl(
                databaseInfo.getHost(),
                databaseInfo.getPort(),
                databaseInfo.getDatabase());
    }

    public String getUrl(String host, String port, String database) {
        return urlPattern.
                replace("<HOST>", CommonUtil.nvl(host, "")).
                replace(":<PORT>", StringUtil.isEmpty(port) ? "" : ":" + port).
                replace("<DATABASE>", CommonUtil.nvl(database, ""));
    }

    public String getDefaultUrl() {
        return getUrl(defaultInfo);
    }

    DatabaseUrlResolver(String urlPattern, String urlRegex, DatabaseInfo defaultInfo) {
        this.urlPattern = urlPattern;
        this.urlRegex = urlRegex;
        this.defaultInfo = defaultInfo;
    }

    @NotNull
    public DatabaseInfo getDefaultInfo() {
        return defaultInfo.clone();
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
