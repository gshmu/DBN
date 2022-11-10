package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class DDLFileType {
    private final DBLanguageFileType languageFileType;
    private DDLFileTypeId id;
    private String description;
    private DBContentType contentType;
    private List<String> extensions = new ArrayList<>();

    public DDLFileType(DDLFileTypeId id, String description, String extension, DBLanguageFileType languageFileType, DBContentType contentType) {
        this.id = id;
        this.description = description;
        this.extensions.add(extension);
        this.languageFileType = languageFileType;
        this.contentType = contentType;
    }

    public boolean setExtensions(List<String> extensions) {
        if (!extensions.containsAll(this.extensions) || !this.extensions.containsAll(extensions)) {
            this.extensions = extensions;
            return true;
        }
        return false;
    }

    public String getExtensionsAsString() {
        return Strings.concatenate(extensions, ", ");
    }

    public boolean setExtensionsAsString(String extensions) {
        return setExtensions(Strings.tokenize(extensions, ","));
    }

}
