package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Strings;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

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
            return FilenameUtils.removeExtension(file.getName());
        }
        return path;
    }

    @Override
    public DatabaseFile clone() {
        return new DatabaseFile(path, schema);
    }

}
