package com.dci.intellij.dbn.common.editor;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;

public interface BasicTextEditor<T extends VirtualFile> extends FileEditor, Disposable {
    Project getProject();

    @NotNull
    Editor getEditor();

    T getVirtualFile();

    boolean canNavigateTo(@NotNull final Navigatable navigatable);

    void navigateTo(@NotNull final Navigatable navigatable);

    EditorProviderId getEditorProviderId();
}
