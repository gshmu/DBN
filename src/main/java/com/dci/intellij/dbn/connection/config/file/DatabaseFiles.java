package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.common.util.Strings;
import com.intellij.openapi.options.ConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void add(DatabaseFile databaseFile) {
        adjustSchemaName(databaseFile);
        files.add(databaseFile);
    }

    public void add(int rowIndex, DatabaseFile databaseFile) {
        if (rowIndex == 0) {
            rowIndex = 1;
        }
        adjustSchemaName(databaseFile);
        files.add(rowIndex, databaseFile);
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

    public void adjustSchemaName(DatabaseFile databaseFile) {
        String path = databaseFile.getPath();
        String schema = databaseFile.getSchema();
        if (Strings.isEmpty(schema) && Strings.isNotEmpty(path)) {
            schema = databaseFile.getFileName();
            Set<String> taken = files.stream().map(f -> f.getSchema()).collect(Collectors.toSet());
            schema = Naming.nextNumberedIdentifier(schema, false, () -> taken);
            databaseFile.setSchema(schema);
        }
    }

    public void validate() throws ConfigurationException {
        Set<String> set = new HashSet<>();
        if (!files.stream().map(file -> file.getPath()).allMatch(e -> set.add(e))) {
            throw new ConfigurationException("Invalid Database files configuration. Duplicate database files.");
        }
        set.clear();
        if (!files.stream().map(file -> file.getSchema()).allMatch(e -> set.add(e))) {
            throw new ConfigurationException("Invalid Database files configuration. Duplicate database identifiers");
        }
    }


    @Override
    public void readConfiguration(Element element) {
        for (Element child : element.getChildren()) {
            String path = stringAttribute(child, "path");
            String schema = stringAttribute(child, "schema");
            DatabaseFile databaseFile = new DatabaseFile(path, schema);
            if (Strings.isEmpty(schema)) {
                adjustSchemaName(databaseFile);
            }
            add(databaseFile);
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
            databaseFiles.files.add(file.clone());
        }
        return databaseFiles;
    }
}
