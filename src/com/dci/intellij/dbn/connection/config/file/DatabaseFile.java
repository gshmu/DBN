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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseFile that = (DatabaseFile) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return schema != null ? schema.equals(that.schema) : that.schema == null;

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }
}
