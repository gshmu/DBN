package com.dci.intellij.dbn.connection.config.file;

import java.io.File;

public class DatabaseFile {
    private File file;
    private String databaseName;

    public DatabaseFile() {
    }

    public DatabaseFile(File file, String databaseName) {
        this.file = file;
        this.databaseName = databaseName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
