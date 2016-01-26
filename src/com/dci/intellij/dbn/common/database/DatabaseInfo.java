package com.dci.intellij.dbn.common.database;


import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseUrlType;

public class DatabaseInfo implements Cloneable{
    public interface Default {
        DatabaseInfo ORACLE   = new DatabaseInfo("localhost", "1521", "XE",       DatabaseUrlType.SID);
        DatabaseInfo MYSQL    = new DatabaseInfo("localhost", "3306", "mysql",    DatabaseUrlType.DATABASE);
        DatabaseInfo POSTGRES = new DatabaseInfo("localhost", "5432", "postgres", DatabaseUrlType.DATABASE);
        DatabaseInfo SQLITE   = new DatabaseInfo("sqlite.db",                     DatabaseUrlType.FILE);
        DatabaseInfo UNKNOWN  = new DatabaseInfo("localhost", "1234", "database", DatabaseUrlType.DATABASE);
    }

    private String host;
    private String port;
    private String database;
    private String file;
    private String url;
    private DatabaseUrlType urlType = DatabaseUrlType.DATABASE;

    public DatabaseInfo() {
    }

    public DatabaseInfo(String host, String port, String database, DatabaseUrlType urlType) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.urlType = urlType;
    }

    public DatabaseInfo(String file, DatabaseUrlType urlType) {
        this.file = file;
        this.urlType = urlType;
    }

    public boolean isEmpty() {
        return StringUtil.isEmpty(host) && StringUtil.isEmpty(port) && StringUtil.isEmpty(database) && StringUtil.isEmpty(file);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public DatabaseUrlType getUrlType() {
        return urlType;
    }

    public void setUrlType(DatabaseUrlType urlType) {
        this.urlType = urlType;
    }

    @Override
    public DatabaseInfo clone() {
        return new DatabaseInfo(host, port, database, urlType);
    }
}
