package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DatabaseUrlPattern {

    ORACLE_SID(
            "jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>:[.\\-$_a-z0-9]{1,1000})$",
            DatabaseInfo.Default.ORACLE,
            DatabaseUrlType.SID),

    ORACLE_SERVICE(
            "jdbc:oracle:thin:@//<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            DatabaseInfo.Default.ORACLE,
            DatabaseUrlType.SERVICE),

    ORACLE_LDAP(
            "jdbc:oracle:thin:@ldap://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@ldap\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            DatabaseInfo.Default.ORACLE,
            DatabaseUrlType.LDAP),

    ORACLE_LDAPS(
            "jdbc:oracle:thin:@ldaps://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@ldaps\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            DatabaseInfo.Default.ORACLE,
            DatabaseUrlType.LDAPS),

    MYSQL(
            "jdbc:mysql://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:mysql:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\.\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.MYSQL,
            DatabaseUrlType.DATABASE),

    POSTGRES(
            "jdbc:postgresql://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:postgresql:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[.\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.POSTGRES,
            DatabaseUrlType.DATABASE),

    REDSHIFT(
            "jdbc:redshift://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:redshift:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[.\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.POSTGRES,
            DatabaseUrlType.DATABASE),

    SQLITE(
            "jdbc:sqlite:<FILE>",
            "^(jdbc:sqlite:)(?<FILE>([a-zA-Z]:)?((\\\\|\\/)[a-zA-Z0-9\\s\\/_\\.-]{1,2000}){1,2000})$",
            DatabaseInfo.Default.SQLITE,
            DatabaseUrlType.FILE),

    GENERIC(
            "jdbc:dbname://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:dbname:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.GENERIC,
            DatabaseUrlType.DATABASE),

    UNKNOWN(
            "jdbc:unknown://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:unknown:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.UNKNOWN,
            DatabaseUrlType.DATABASE),
    ;

    private DatabaseUrlType urlType;
    private String urlPattern;
    private String urlRegex;
    private DatabaseInfo defaultInfo;

    public static DatabaseUrlPattern get(@NotNull DatabaseType databaseType, @NotNull DatabaseUrlType urlType) {
        for (DatabaseUrlPattern urlPattern : values()) {
            if (databaseType.hasUrlPattern(urlPattern) && urlPattern.getUrlType() == urlType) {
                return urlPattern;
            }
        }
        return databaseType.getDefaultUrlPattern();
    }


    public String getUrl(DatabaseInfo databaseInfo) {
        return getUrl(
                databaseInfo.getHost(),
                databaseInfo.getPort(),
                databaseInfo.getDatabase(),
                databaseInfo.getMainFile());
    }

    public String getUrl(String host, String port, String database, String file) {
        return urlPattern.
                replace("<HOST>", CommonUtil.nvl(host, "")).
                replace(":<PORT>", StringUtil.isEmpty(port) ? "" : ":" + port).
                replace("<DATABASE>", CommonUtil.nvl(database, "")).
                replace("<FILE>", CommonUtil.nvl(file, ""));
    }

    public String getDefaultUrl() {
        return getUrl(defaultInfo);
    }

    public DatabaseUrlType getUrlType() {
        return urlType;
    }

    DatabaseUrlPattern(String urlPattern, String urlRegex, DatabaseInfo defaultInfo, DatabaseUrlType urlType) {
        this.urlPattern = urlPattern;
        this.urlRegex = urlRegex;
        this.defaultInfo = defaultInfo;
        this.urlType = urlType;
    }

    @NotNull
    public DatabaseInfo getDefaultInfo() {
        return defaultInfo.clone();
    }

    public String resolveHost(String url) {
        if (urlType != DatabaseUrlType.FILE) {
            if (StringUtil.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    return matcher.group("HOST");
                }
            }
        }
        return "";
    }

    public String resolvePort(String url) {
        if (urlType != DatabaseUrlType.FILE) {
            if (StringUtil.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String portGroup = matcher.group("PORT");
                    if (StringUtil.isNotEmpty(portGroup)) {
                        return portGroup.substring(1);
                    }
                }
            }
        }
        return "";
    }

    public String resolveDatabase(String url) {
        if (urlType != DatabaseUrlType.FILE) {
            if (StringUtil.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String databaseGroup = matcher.group("DATABASE");
                    if (StringUtil.isNotEmpty(databaseGroup)) {
                        return databaseGroup.substring(1);
                    }
                }
            }
        }

        return "";
    }

    public String resolveFile(String url) {
        if (urlType == DatabaseUrlType.FILE) {
            if (StringUtil.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String fileGroup = matcher.group("FILE");
                    if (StringUtil.isNotEmpty(fileGroup)) {
                        return fileGroup;
                    }
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

    public boolean matches(String url) {
        return isValid(url);
    }
}
