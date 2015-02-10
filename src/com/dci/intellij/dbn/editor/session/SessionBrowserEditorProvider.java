package com.dci.intellij.dbn.editor.session;

import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.data.state.DatasetEditorState;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class SessionBrowserEditorProvider implements FileEditorProvider, ApplicationComponent, DumbAware {
    /*********************************************************
     *                  FileEditorProvider                   *
     *********************************************************/

    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile instanceof DBSessionBrowserVirtualFile;
    }

    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
        return new SessionBrowser(sessionBrowserFile);
    }

    public void disposeEditor(@NotNull final FileEditor editor) {
        Disposer.dispose(editor);
    }

    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
            DBSchemaObject object = editableObjectFile.getObject();
            if (object instanceof DBDataset) {
                DatasetEditorState editorState = new DatasetEditorState();
                editorState.readState(sourceElement);
                return editorState;

            }
        }
        return new DatasetEditorState();
    }

    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (state instanceof DatasetEditorState) {
            DatasetEditorState editorState = (DatasetEditorState) state;
            editorState.writeState(targetElement);
        }
    }

    @NotNull
    @NonNls
    public String getEditorTypeId() {
        return EditorProviderId.SESSION_BROWSER.getId();
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.SessionBrowserProvider";
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }
}

