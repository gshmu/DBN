package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SourceCodeBodyEditorProvider extends BasicSourceCodeEditorProvider{

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        DBContentType contentType = FailsafeUtil.lenient(() -> {
            DBEditableObjectVirtualFile databaseFile = null;
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                databaseFile = (DBEditableObjectVirtualFile) virtualFile;
            }

/*
            else if (virtualFile instanceof SourceCodeFile) {
                SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
                databaseFile = sourceCodeFile.getDatabaseFile();
            }
*/
            return databaseFile == null ? null : databaseFile.getObject().getContentType();
        });

        return contentType == DBContentType.CODE_SPEC_AND_BODY;
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.CODE_BODY;
    }

    @Override
    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;

    }

    @NotNull
    @Override
    public EditorProviderId getEditorProviderId() {
        return EditorProviderId.CODE_BODY;
    }

    @Override
    public String getName() {
        return "Body";
    }

    @Override
    public Icon getIcon() {
        return null;//Icons.CODE_EDITOR_BODY;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DBSourceBodyEditorProvider";
    }
}
