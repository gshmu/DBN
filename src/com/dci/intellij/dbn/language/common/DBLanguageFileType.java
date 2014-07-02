package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.dci.intellij.dbn.vfs.SourceCodeFile;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class DBLanguageFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
    protected String extension;
    protected String description;
    protected DBContentType contentType;

    public DBLanguageFileType(@NotNull Language language,
                      String extension,
                      String description,
                      DBContentType contentType) {
        super(language);
        this.extension = extension;
        this.description = description;
        this.contentType = contentType;
    }

    public void setExtension(String extension) {
        if (!this.extension.equals(extension)) {
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            fileTypeManager.removeAssociatedExtension(this, this.extension);
            this.extension = extension;
            fileTypeManager.registerFileType(this, extension);
        }
    }

    public DBContentType getContentType() {
        return contentType;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getDefaultExtension() {
        return extension;
    }


    public boolean isMyFileType(VirtualFile file) {
        if (file instanceof DatabaseEditableObjectFile || file instanceof SourceCodeFile) {
            if (this == file.getFileType()) {
                return true;
            }
        }

        if (file instanceof SQLConsoleFile) {
            return true;
        }
        return false;
    }
}
