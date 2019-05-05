package com.dci.intellij.dbn.common.database;


import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;

public class DatabaseInfo implements Cloneable<DatabaseInfo> {
    public interface Default {
        DatabaseInfo ORACLE   = new DatabaseInfo("oracle", "localhost", "1521", "XE",       DatabaseUrlType.SID);
        DatabaseInfo MYSQL    = new DatabaseInfo("mysql", "localhost", "3306", "mysql",    DatabaseUrlType.DATABASE);
        DatabaseInfo POSTGRES = new DatabaseInfo("postgresql", "localhost", "5432", "postgres", DatabaseUrlType.DATABASE);
        DatabaseInfo SQLITE   = new DatabaseInfo("sqlite", "sqlite.db",                     DatabaseUrlType.FILE);
        DatabaseInfo GENERIC  = new DatabaseInfo("dbtype", "localhost", "1234", "database", DatabaseUrlType.DATABASE);
    }

    private String vendor;
    private String host;
    private String port;
    private String database;
    private String url;
    private DatabaseFiles files;
    private DatabaseUrlType urlType = DatabaseUrlType.DATABASE;

    private DatabaseInfo() {}

    public DatabaseInfo(String vendor, String host, String port, String database, DatabaseUrlType urlType) {
        this.vendor = vendor;
        this.host = host;
        this.port = port;
        this.database = database;
        this.urlType = urlType;
    }

    public DatabaseInfo(String vendor, String file, DatabaseUrlType urlType) {
        this.vendor = vendor;
        this.files = new DatabaseFiles(file);
        this.urlType = urlType;
    }



    public boolean isEmpty() {
        return StringUtil.isEmpty(host) && StringUtil.isEmpty(port) && StringUtil.isEmpty(database) && (files == null || StringUtil.isEmpty(files.getMainFile().getPath()));
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getVendor() {
        return vendor;
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


    public DatabaseUrlType getUrlType() {
        return urlType;
    }

    public void setUrlType(DatabaseUrlType urlType) {
        this.urlType = urlType;
    }

    public DatabaseFiles getFiles() {
        return files;
    }

    public String getMainFile() {
        return files == null ? null : files.getMainFile().getPath();
    }

    public String getFilesForHash() {
        if (files != null) {
            StringBuilder builder = new StringBuilder();
            for (DatabaseFile databaseFile : files.getFiles()) {
                if (builder.length() > 0) {
                    builder.append("#");
                }
                builder.append(databaseFile.getPath()).append("@").append(databaseFile.getSchema());
            }
            return builder.toString();

        }
        return null;
    }

    public void setMainFile(String mainFile) {
        if (files == null) {
            files = new DatabaseFiles(mainFile);
        } else {
            files.getMainFile().setPath(mainFile);
        }
    }

    public void setFiles(DatabaseFiles files) {
        this.files = files;
    }

    @Override
    public DatabaseInfo clone() {
        DatabaseInfo clone = new DatabaseInfo();
        clone.vendor = vendor;
        clone.host = host;
        clone.port = port;
        clone.database = database;
        clone.url = url;
        clone.files = files == null ? null : files.clone();
        clone.urlType = urlType;
        return clone;
    }
}
