package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Strings;
import lombok.Data;

import java.io.File;

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

    public String getFileName() {
        if (Strings.isNotEmpty(path)) {
            File file = new File(path);
            String name = file.getName();
            int index = name.lastIndexOf(".");
            if (index > -1) {
                return name.substring(0, index);
            }
            return name;
        }
        return path;
    }

    @Override
    public DatabaseFile clone() {
        return new DatabaseFile(path, schema);
    }

}
