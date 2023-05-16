package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.util.Strings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dci.intellij.dbn.common.util.Commons.nvl;

@Getter
public enum DatabaseUrlPattern {

    ORACLE_TNS(
            "jdbc:oracle:thin:@<TNS_CONNECTION_PROFILE>?TNS_ADMIN=<TNS_FOLDER>",
            // TODO: url pattern
            "^jdbc:oracle:thin:@.*$",
            DatabaseInfo.Default.ORACLE,
            DatabaseUrlType.TNS),

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
            "^(jdbc:sqlite:)(?<FILE>([a-zA-Z]:)?((\\\\|\\/)[a-zA-Z0-9\\s\\/_\\.\\-']{1,2000}){1,2000})$",
            DatabaseInfo.Default.SQLITE,
            DatabaseUrlType.FILE),

    GENERIC(
            "jdbc:<VENDOR>://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:(?<VENDOR>[._\\-a-z0-9]{1,1000}):\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\-$_a-z0-9]{0,1000})?$",
            DatabaseInfo.Default.GENERIC,
            DatabaseUrlType.CUSTOM),
    ;

    private final DatabaseUrlType urlType;
    private final String urlPattern;
    private final String urlRegex;
    private final DatabaseInfo defaultInfo;

    public static DatabaseUrlPattern get(@NotNull DatabaseType databaseType, @NotNull DatabaseUrlType urlType) {
        for (DatabaseUrlPattern urlPattern : values()) {
            if (databaseType.hasUrlPattern(urlPattern) && urlPattern.getUrlType() == urlType) {
                return urlPattern;
            }
        }
        return databaseType.getDefaultUrlPattern();
    }


    public String buildUrl(DatabaseInfo databaseInfo) {
        if (databaseInfo.getUrlType() == DatabaseUrlType.TNS) {
            return buildUrl(
                    databaseInfo.getTnsFolder(),
                    databaseInfo.getTnsProfile());
        } else {
            return buildUrl(
                    databaseInfo.getVendor(),
                    databaseInfo.getHost(),
                    databaseInfo.getPort(),
                    databaseInfo.getDatabase(),
                    databaseInfo.getMainFile());
        }
    }

    public String buildUrl(String tnsFolder, String tnsProfile) {
    	return urlPattern.replace("<TNS_FOLDER>", nvl(tnsFolder, ""))
				 .replace("<TNS_CONNECTION_PROFILE>", nvl(tnsProfile, ""));
    }

    public String buildUrl(String vendor, String host, String port, String database, String file) {
        return urlPattern.
                replace("<VENDOR>", nvl(vendor, "")).
                replace("<HOST>", nvl(host, "")).
                replace(":<PORT>", Strings.isEmpty(port) ? "" : ":" + port).
                replace("<DATABASE>", nvl(database, "")).
                replace("<FILE>", nvl(file, ""));
    }

    public String getDefaultUrl() {
        return buildUrl(defaultInfo);
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
            if (Strings.isNotEmpty(url)) {
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
            if (Strings.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String portGroup = matcher.group("PORT");
                    if (Strings.isNotEmpty(portGroup)) {
                        return portGroup.substring(1);
                    }
                }
            }
        }
        return "";
    }

    public String resolveDatabase(String url) {
        if (urlType != DatabaseUrlType.FILE) {
            if (Strings.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String databaseGroup = matcher.group("DATABASE");
                    if (Strings.isNotEmpty(databaseGroup)) {
                        return databaseGroup.substring(1);
                    }
                }
            }
        }

        return "";
    }

    public String resolveFile(String url) {
        if (urlType == DatabaseUrlType.FILE) {
            if (Strings.isNotEmpty(url)) {
                Matcher matcher = getMatcher(url);
                if (matcher.matches()) {
                    String fileGroup = matcher.group("FILE");
                    if (Strings.isNotEmpty(fileGroup)) {
                        return fileGroup;
                    }
                }
            }
        }
        return "";
    }

    public boolean isValid(String url) {
        if (Strings.isNotEmpty(url)) {
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
