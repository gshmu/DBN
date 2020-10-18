package com.dci.intellij.dbn.language.editor;

import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.editor.ui.DBLanguageFileEditorToolbarForm;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DBLanguageFileEditorListener implements FileEditorManagerListener{
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if ((file.isInLocalFileSystem() || file instanceof LightVirtualFile) && file.getFileType() instanceof DBLanguageFileType) {
            FileEditor fileEditor = source.getSelectedEditor(file);
            if (fileEditor != null) {
                DBLanguageFileEditorToolbarForm toolbarForm = new DBLanguageFileEditorToolbarForm(fileEditor, source.getProject(), file);
                fileEditor.getComponent().add(toolbarForm.getComponent(), BorderLayout.NORTH);
                fileEditor.putUserData(DBLanguageFileEditorToolbarForm.USER_DATA_KEY, toolbarForm);
            }
        }
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if ((file.isInLocalFileSystem() || file instanceof LightVirtualFile) && file.getFileType() instanceof DBLanguageFileType) {
            FileEditor editor = source.getSelectedEditor(file);
            if (editor != null) {
                DBLanguageFileEditorToolbarForm toolbarForm = editor.getUserData(DBLanguageFileEditorToolbarForm.USER_DATA_KEY);
                if (toolbarForm != null) {
                    toolbarForm.dispose();
                }
            }
        }

    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    }
}
