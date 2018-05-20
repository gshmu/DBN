package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;

import java.util.ArrayList;
import java.util.List;

public class DDLFileType {
    private DBLanguageFileType languageFileType;
    private DDLFileTypeId id;
    private String description;
    private DBContentType contentType;
    private List<String> extensions = new ArrayList<String>();

    public DDLFileType(DDLFileTypeId id, String description, String extension, DBLanguageFileType languageFileType, DBContentType contentType) {
        this.id = id;
        this.description = description;
        this.extensions.add(extension);
        this.languageFileType = languageFileType;
        this.contentType = contentType;
    }

    public DBLanguageFileType getLanguageFileType() {
        return languageFileType;
    }

    public DDLFileTypeId getId() {
        return id;
    }

    public void setId(DDLFileTypeId id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public boolean setExtensions(List<String> extensions) {
        if (!extensions.containsAll(this.extensions) || !this.extensions.containsAll(extensions)) {
            this.extensions = extensions;
            return true;
        }
        return false;
    }

    public DBContentType getContentType() {
        return contentType;
    }

    public void setContentType(DBContentType contentType) {
        this.contentType = contentType;
    }

    public String getExtensionsAsString() {
        return StringUtil.concatenate(extensions, ", ");
    }

    public boolean setExtensionsAsString(String extensions) {
        return setExtensions(StringUtil.tokenize(extensions, ","));
    }

}
