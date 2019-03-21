package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setStringAttribute;

public class DatabaseFiles implements PersistentConfiguration, Cloneable<DatabaseFiles> {
    private List<DatabaseFile> files = new ArrayList<DatabaseFile>();

    public DatabaseFiles() {
    }

    public DatabaseFiles(String mainFile) {
        files.add(new DatabaseFile(mainFile, "main"));
    }

    public List<DatabaseFile> getFiles() {
        return files;
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
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String path = child.getAttributeValue("path");
            String schema = child.getAttributeValue("schema");
            DatabaseFile databaseFile = new DatabaseFile(path, schema);
            files.add(databaseFile);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        for (DatabaseFile file : files) {
            String path = file.getPath();
            String schema = file.getSchema();
            if (StringUtil.isNotEmpty(path) || StringUtil.isNotEmpty(schema)) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseFiles that = (DatabaseFiles) o;

        return files.equals(that.files);

    }

    @Override
    public int hashCode() {
        return files.hashCode();
    }
}
