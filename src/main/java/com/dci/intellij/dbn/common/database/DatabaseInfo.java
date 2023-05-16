package com.dci.intellij.dbn.common.database;


import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
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
	private String tnsFolder;
	private String tnsProfile;

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
        return Strings.isEmpty(host) && Strings.isEmpty(port) && Strings.isEmpty(database) && (files == null || Strings.isEmpty(files.getMainFile().getPath()));
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

    @Override
    public DatabaseInfo clone() {
        DatabaseInfo clone = new DatabaseInfo();
        clone.vendor = this.vendor;
        clone.host = this.host;
        clone.port = this.port;
        clone.database = this.database;
        clone.url = this.url;
        clone.files = this.files == null ? null : this.files.clone();
        clone.urlType = this.urlType;
        clone.tnsFolder = this.tnsFolder;
        clone.tnsProfile = this.tnsProfile;

        return clone;
    }

}
