package com.dci.intellij.dbn.data.editor.text;

import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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

    public static TextContentType get(Project project, String contentTypeName) {
        DataEditorQualifiedEditorSettings qualifiedEditorSettings = DataEditorSettings.getInstance(project).getQualifiedEditorSettings();
        TextContentType contentType = qualifiedEditorSettings.getContentType(contentTypeName);
        return contentType == null ? getPlainText(project) : contentType;
    }

    public static TextContentType getPlainText(Project project) {
        return get(project, "Text");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getError() {
        return null;
    }

    public FileType getFileType() {
        return fileType;
    }

    @Override
    public Icon getIcon() {
        return fileType.getIcon();
    }

    @Override
    public boolean isSelected() {
        return enabled;
    }

    @Override
    public boolean isMasterSelected() {
        return true;
    }

    @Override
    public void setSelected(boolean selected) {
        this.enabled = selected;
    }

    @Override
    public int compareTo(@NotNull TextContentType remote) {
        return name.compareTo(remote.name);
    }
}

