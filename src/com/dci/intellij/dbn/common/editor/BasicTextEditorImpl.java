package com.dci.intellij.dbn.common.editor;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.editor.EditorProviderId;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;

public abstract class BasicTextEditorImpl<T extends VirtualFile> implements BasicTextEditor<T>{
    protected TextEditor textEditor;
    private T virtualFile;
    private String name;
    private Project project;
    private EditorProviderId editorProviderId;
    private BasicTextEditorState cachedState;

    public BasicTextEditorImpl(Project project, T virtualFile, String name, EditorProviderId editorProviderId) {
        this.project = project;
        this.name = name;
        this.virtualFile = virtualFile;
        this.editorProviderId = editorProviderId;
        textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, virtualFile);
        Disposer.register(this, textEditor);
    }

    public Project getProject() {
        return project;
    }

    public T getVirtualFile() {
        return virtualFile;
    }

    public <T> T getUserData(@NotNull Key<T> key) {
        return textEditor.getUserData(key);
    }

    public <T> void putUserData(@NotNull Key<T> key, T value) {
        textEditor.putUserData(key, value);
    }

    public boolean isModified() {
        return textEditor.isModified();
    }

    public boolean isValid() {
        return textEditor.isValid();
    }

    public void selectNotify() {
        textEditor.selectNotify();
    }

    public void deselectNotify() {
        textEditor.deselectNotify();
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }

    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return textEditor.getBackgroundHighlighter();
    }

    public FileEditorLocation getCurrentLocation() {
        return textEditor.getCurrentLocation();
    }

    @NotNull
    public Editor getEditor() {
        return textEditor.getEditor();
    }

    public boolean canNavigateTo(@NotNull final Navigatable navigatable) {
        return textEditor.canNavigateTo(navigatable);
    }

    public void navigateTo(@NotNull final Navigatable navigatable) {
        textEditor.navigateTo(navigatable);
    }

    @NotNull
    public JComponent getComponent() {
        return textEditor.getComponent();
    }

    @Override
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return textEditor.getPreferredFocusedComponent();
    }

    protected BasicTextEditorState createEditorState() {
        return new BasicTextEditorState();
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        if (!isDisposed()) {
            cachedState = createEditorState();
            cachedState.loadFromEditor(level, textEditor);
        }
        return cachedState;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof BasicTextEditorState) {
            BasicTextEditorState state = (BasicTextEditorState) fileEditorState;
            state.applyToEditor(textEditor);
        }
    }

    @NonNls
    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return textEditor.getStructureViewBuilder();
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        project = null;
        virtualFile = null;
        textEditor = null;
    }
}
