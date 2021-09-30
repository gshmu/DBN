package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public abstract class DBLanguageFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
    protected String extension;
    protected String description;
    protected DBContentType contentType;

    public DBLanguageFileType(
            @NotNull Language language,
            @NotNull String extension,
            @NotNull String description,
            @NotNull DBContentType contentType) {
        super(language);
        this.extension = extension;
        this.description = description;
        this.contentType = contentType;
    }

    public void setExtension(String extension) {
        if (!Objects.equals(this.extension, extension)) {
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            fileTypeManager.removeAssociatedExtension(this, this.extension);
            this.extension = extension;
            fileTypeManager.registerFileType(this, extension);
        }
    }

    @Override
    @NotNull
    public String getDefaultExtension() {
        return extension;
    }

    @Override
    public boolean isMyFileType(@NotNull VirtualFile file) {
        if (file instanceof DBEditableObjectVirtualFile || file instanceof DBSourceCodeVirtualFile) {
            if (this == file.getFileType()) {
                return true;
            }
        }

        if (file instanceof DBConsoleVirtualFile) {
            return true;
        }
        return false;
    }
}
