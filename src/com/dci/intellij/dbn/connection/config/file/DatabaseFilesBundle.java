package com.dci.intellij.dbn.connection.config.file;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.file.ui.DatabaseFileSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFilesBundle extends Configuration<DatabaseFileSettingsForm>{
    private List<DatabaseFile> files = new ArrayList<DatabaseFile>();

    public DatabaseFilesBundle(List<DatabaseFile> files) {
        this.files = files;
    }

    public DatabaseFilesBundle() {
        files.add(new DatabaseFile(null, "main"));
    }

    @NotNull
    @Override
    protected DatabaseFileSettingsForm createConfigurationEditor() {
        return new DatabaseFileSettingsForm(this);
    }

    public List<DatabaseFile> getFiles() {
        return files;
    }

    public void setFiles(List<DatabaseFile> files) {
        this.files = files;
    }

    @Override
    public void readConfiguration(Element element) {

    }

    @Override
    public void writeConfiguration(Element element) {

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
        files.add(rowIndex, filePathOption);
    }

    public void remove(int rowIndex) {
        files.remove(rowIndex);
    }
}
