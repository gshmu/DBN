package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.util.Cloneable;
import lombok.Data;

@Data
public class DatabaseFile implements Cloneable<DatabaseFile> {
    private String path;
    private String schema;

    public DatabaseFile() {
    }

    public DatabaseFile(String path, String schema) {
        this.path = path;
        this.schema = schema;
    }

    @Override
    public DatabaseFile clone() {
        return new DatabaseFile(path, schema);
    }

}
