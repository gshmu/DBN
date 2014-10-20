package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

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

    protected void updateTabIcon(final DBEditableObjectVirtualFile databaseFile, final BasicTextEditor textEditor, final Icon icon) {
        new SimpleLaterInvocator() {
            public void execute() {
                Project project = databaseFile.getProject();
                if (project != null) {
                    EditorUtil.setEditorIcon(project, databaseFile, textEditor, icon);
                }
            }
        }.start();
    }

    @NotNull
    @Override
    public final String getEditorTypeId() {
        return getEditorProviderId().getId();
    }

    @NotNull
    public abstract EditorProviderId getEditorProviderId();


    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/
    public void initComponent() {
    }

    public void disposeComponent() {

    }
}
