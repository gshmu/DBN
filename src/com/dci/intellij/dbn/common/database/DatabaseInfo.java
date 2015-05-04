package com.dci.intellij.dbn.common.database;


import com.dci.intellij.dbn.common.util.StringUtil;

public class DatabaseInfo implements Cloneable{
    public static final DatabaseInfo ORACLE = new DatabaseInfo("localhost", "1521", "XE");
    public static final DatabaseInfo MYSQL = new DatabaseInfo("localhost", "3306", "mysql");
    public static final DatabaseInfo POSTGRES = new DatabaseInfo("localhost", "5432", "postgres");
    public static final DatabaseInfo UNKNOWN = new DatabaseInfo("localhost", "1234", "database");

    private String host;
    private String port;
    private String database;

    public DatabaseInfo() {
    }

    public DatabaseInfo(String host, String port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }

    public boolean isEmpty() {
        return StringUtil.isEmpty(host) && StringUtil.isEmpty(port) && StringUtil.isEmpty(database);
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

    @Override
    public DatabaseInfo clone() {
        return new DatabaseInfo(host, port, database);
    }
}
