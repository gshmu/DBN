package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setStringAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@EqualsAndHashCode
public class DatabaseFiles implements PersistentConfiguration, Cloneable<DatabaseFiles> {
    private final List<DatabaseFile> files = new ArrayList<>();

    public DatabaseFiles() {
    }

    public DatabaseFiles(String mainFile) {
        files.add(new DatabaseFile(mainFile, "main"));
    }

    public int size() {
        return files.size();
    }

    public DatabaseFile get(int rowIndex) {
        return files.get(rowIndex);
    }

    public void add(DatabaseFile filePathOption) {
        files.add(filePathOption);
    }

    public void add(int rowIndex, DatabaseFile filePathOption) {
        if (rowIndex == 0) {
            rowIndex = 1;
        }
        files.add(rowIndex, filePathOption);
    }

    public void remove(int rowIndex) {
        if (rowIndex != 0) {
            files.remove(rowIndex);
        }
    }

    public DatabaseFile getMainFile() {
        return files.get(0);
    }

    public List<DatabaseFile> getSecondaryFiles() {
        return files.subList(1, files.size());
    }


    @Override
    public void readConfiguration(Element element) {
        for (Element child : element.getChildren()) {
            String path = stringAttribute(child, "path");
            String schema = stringAttribute(child, "schema");
            DatabaseFile databaseFile = new DatabaseFile(path, schema);
            files.add(databaseFile);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        for (DatabaseFile file : files) {
            String path = file.getPath();
            String schema = file.getSchema();
            if (Strings.isNotEmpty(path) || Strings.isNotEmpty(schema)) {
                Element child = new Element("file");
                setStringAttribute(child, "path", path);
                setStringAttribute(child, "schema", schema);
                element.addContent(child);
            }
        }
    }

    @Override
    public DatabaseFiles clone() {
        DatabaseFiles databaseFiles = new DatabaseFiles();
        for (DatabaseFile file : files) {
            databaseFiles.add(file.clone());
        }
        return databaseFiles;
    }
}
