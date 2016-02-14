package com.dci.intellij.dbn.connection.config.file;

public class DatabaseFile implements com.dci.intellij.dbn.common.util.Cloneable<DatabaseFile>{
    private String path;
    private String schema;

    public DatabaseFile() {
    }

    public DatabaseFile(String path, String schema) {
        this.path = path;
        this.schema = schema;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public DatabaseFile clone() {
        return new DatabaseFile(path, schema);
    }
}
