package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SourceCodeEditorProvider extends BasicSourceCodeEditorProvider {

    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DatabaseEditableObjectFile) {
            DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) virtualFile;
            DBContentType contentType = databaseFile.getObject().getContentType();
            return contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_AND_DATA);

        }
        return false;
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.CODE;
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;

    }

    @NotNull
    @NonNls
    public String getEditorTypeId() {
        return "0";
    }

    public String getName() {
        return "Code";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DBSourceEditorProvider";
    }

}
