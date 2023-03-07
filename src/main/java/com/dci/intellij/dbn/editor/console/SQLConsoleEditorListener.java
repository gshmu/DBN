package com.dci.intellij.dbn.editor.console;

import com.dci.intellij.dbn.editor.console.ui.SQLConsoleEditorToolbarForm;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.util.Files.isDbConsoleFile;

public class SQLConsoleEditorListener implements FileEditorManagerListener{
    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (isNotValid(file)) return;
        if (!isDbConsoleFile(file)) return;

        SQLConsoleEditor fileEditor = (SQLConsoleEditor) source.getSelectedEditor(file);
        if (isNotValid(fileEditor)) return;

        SQLConsoleEditorToolbarForm toolbarForm = new SQLConsoleEditorToolbarForm(source.getProject(), fileEditor);
        fileEditor.getComponent().getParent().add(toolbarForm.getComponent(), BorderLayout.NORTH);
    }
}
