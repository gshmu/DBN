package com.dci.intellij.dbn.common.editor;

import javax.swing.Icon;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectVirtualFile;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class BasicTextEditorProvider implements FileEditorProvider, ApplicationComponent, DumbAware {
    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull final Project project, @NotNull final VirtualFile virtualFile) {
        BasicTextEditorState editorState = new BasicTextEditorState();
        editorState.readState(sourceElement, project, virtualFile);
        return editorState;
    }

    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (state instanceof BasicTextEditorState) {
            BasicTextEditorState editorState = (BasicTextEditorState) state;
            editorState.writeState(targetElement, project);
        }
    }

    protected void updateTabIcon(final DatabaseEditableObjectVirtualFile databaseFile, final BasicTextEditor textEditor, final Icon icon) {
        new SimpleLaterInvocator() {
            public void execute() {
                EditorUtil.setEditorIcon(databaseFile, textEditor, icon);
            }
        }.start();
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/
    public void initComponent() {
    }

    public void disposeComponent() {

    }
}
