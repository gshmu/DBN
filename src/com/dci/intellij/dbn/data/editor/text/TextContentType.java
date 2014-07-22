package com.dci.intellij.dbn.data.editor.text;

import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;

public class TextContentType implements Selectable<TextContentType> {
    private String name;
    private FileType fileType;
    private boolean enabled = true;

    public TextContentType(String name, FileType fileType) {
        this.name = name;
        this.fileType = fileType;
    }

    @Nullable
    public static TextContentType create(String name, String fileTypeName) {
        FileType fileType = FileTypeManager.getInstance().getStdFileType(fileTypeName);
        // if returned expected file type
        if (fileType.getName().equals(fileTypeName)) {
            return new TextContentType(name, fileType);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getError() {
        return null;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Icon getIcon() {
        return fileType.getIcon();
    }

    public boolean isSelected() {
        return enabled;
    }

    public boolean isMasterSelected() {
        return true;
    }

    public void setSelected(boolean selected) {
        this.enabled = selected;
    }

    @Override
    public int compareTo(@NotNull TextContentType remote) {
        return name.compareTo(remote.name);
    }
}

