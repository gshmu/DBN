package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo.Default;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.connection.DatabaseUrlType.*;
import static com.intellij.openapi.util.text.StringUtil.isEmpty;

@Slf4j
@Getter
public enum DatabaseUrlPattern {

    ORACLE_TNS(
            "jdbc:oracle:thin:@<TNS_PROFILE>?TNS_ADMIN=<TNS_FOLDER>",
            "^jdbc:oracle:thin:@(?<PROFILE>([a-zA-Z0-9._\\-])+)\\?TNS_ADMIN=(?<FOLDER>([a-zA-Z]:)?(([\\\\/])[a-zA-Z0-9\\s/_.\\-']{1,2000}){1,2000})$",
            Default.ORACLE, TNS),

    ORACLE_SID(
            "jdbc:oracle:thin:@<HOST>:<PORT>:<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>:[.\\-$_a-z0-9]{1,1000})$",
            Default.ORACLE, SID),

    ORACLE_SERVICE(
            "jdbc:oracle:thin:@//<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            Default.ORACLE, SERVICE),


    ORACLE_LDAP(
            "jdbc:oracle:thin:@ldap://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@ldap\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            Default.ORACLE, LDAP),

    ORACLE_LDAPS(
            "jdbc:oracle:thin:@ldaps://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:oracle:(?:thin|oci):@ldaps\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})(?<DATABASE>\\/[.\\-$_a-z0-9]{1,1000})$",
            Default.ORACLE, LDAPS),

    MYSQL_DB(
            "jdbc:mysql://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:mysql:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\.\\-$_a-z0-9]{0,1000})?$",
            Default.MYSQL, DATABASE),

    POSTGRES_DB(
            "jdbc:postgresql://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:postgresql:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[.\\-$_a-z0-9]{0,1000})?$",
            Default.POSTGRES, DATABASE),

    REDSHIFT_DB(
            "jdbc:redshift://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:redshift:\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[.\\-$_a-z0-9]{0,1000})?$",
            Default.POSTGRES, DATABASE),

    SQLITE_FILE(
            "jdbc:sqlite:<FILE>",
            "^(jdbc:sqlite:)(?<FILE>([a-zA-Z]:)?((\\\\|\\/)[a-zA-Z0-9\\s\\/_\\.\\-']{1,2000}){1,2000})?$",
            Default.SQLITE, FILE),

    GENERIC(
            "jdbc:<VENDOR>://<HOST>:<PORT>/<DATABASE>",
            "^(jdbc:(?<VENDOR>[._\\-a-z0-9]{1,1000}):\\/\\/)(?<HOST>[._\\-a-z0-9]{1,1000})(?<PORT>:[0-9]{1,100})?(?<DATABASE>\\/[\\-$_a-z0-9]{0,1000})?$",
            Default.GENERIC, CUSTOM),
    ;

    private final DatabaseUrlType urlType;
    private final String urlPattern;
    private final String urlRegex;
    private final DatabaseInfo defaultInfo;

    public static DatabaseUrlPattern get(@NotNull DatabaseType databaseType, @NotNull DatabaseUrlType urlType) {
        for (DatabaseUrlPattern urlPattern : values()) {
            if (databaseType.supportsUrlPattern(urlPattern) && urlPattern.getUrlType() == urlType) {
                return urlPattern;
            }
        }
        return databaseType.getDefaultUrlPattern();
    }


    public String buildUrl(DatabaseInfo databaseInfo) {
        return buildUrl(
                databaseInfo.getVendor(),
                databaseInfo.getHost(),
                databaseInfo.getPort(),
                databaseInfo.getDatabase(),
                databaseInfo.getMainFilePath(),
                databaseInfo.getTnsFolder(),
                databaseInfo.getTnsProfile());
    }

    public String buildUrl(String vendor, String host, String port, String database, String file, String tnsFolder, String tnsProfile) {
        return urlPattern.
                replace("<VENDOR>", nvl(vendor, "")).
                replace("<HOST>", nvl(host, "")).
                replace(":<PORT>", isEmpty(port) ? "" : ":" + port).
                replace("<DATABASE>", nvl(database, "")).
                replace("<FILE>", nvl(file, "")).
                replace("<TNS_FOLDER>", nvl(tnsFolder, "")).replaceAll("\\\\", "/").
                replace("<TNS_PROFILE>", nvl(tnsProfile, ""));
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
        return resolveGroup(url, "HOST", false, DATABASE, SERVICE, SID, LDAP, LDAPS);
    }

    public String resolvePort(String url) {
        return resolveGroup(url, "PORT", true, DATABASE, SERVICE, SID, LDAP, LDAPS);
    }

    public String resolveDatabase(String url) {
        return resolveGroup(url, "DATABASE", true, DATABASE, SERVICE, SID, LDAP, LDAPS);
    }

    public String resolveFile(String url) {
        return resolveGroup(url, "FILE", false, FILE);
    }

    public String resolveTnsFolder(String url) {
        return resolveGroup(url, "TNS_FOLDER", false, TNS);
    }

    public String resolveTnsProfile(String url) {
        return resolveGroup(url, "TNS_PROFILE", false, TNS);
    }

    public boolean isValid(String url) {
        if (isEmpty(url)) return false;

        Matcher matcher = getMatcher(url);
        return matcher.matches();
    }

    @NotNull
    private Matcher getMatcher(String url) {
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(url);
    }

    private String resolveGroup(String url, String name, boolean shift, DatabaseUrlType ... urlTypes) {
        if (!urlType.isOneOf(urlTypes)) return "";
        if (!isValid(url)) return "";

        try {
            Matcher matcher = getMatcher(url);
            String group = matcher.group(name).trim();
            if (shift) {
                group = isEmpty(group) ? "" : group.substring(1);
            }

            return group;
        } catch (Exception e) {
            log.error("Failed to get group {} from url \"{}\"", name, url);
            return "";
        }
    }

    public boolean matches(String url) {
        return isValid(url);
    }
}
