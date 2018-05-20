package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public abstract class BasicTextEditorImpl<T extends VirtualFile> extends DisposableBase implements BasicTextEditor<T>{
    protected TextEditor textEditor;
    private T virtualFile;
    private String name;
    private EditorProviderId editorProviderId;
    private BasicTextEditorState cachedState;
    private Project project;

    public BasicTextEditorImpl(Project project, T virtualFile, String name, EditorProviderId editorProviderId) {
        this.project = project;
        this.name = name;
        this.virtualFile = virtualFile;
        this.editorProviderId = editorProviderId;
        textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, virtualFile);
        Disposer.register(this, textEditor);
    }

    @NotNull
    public T getVirtualFile() {
        return FailsafeUtil.get(virtualFile);
    }

    public <D> D getUserData(@NotNull Key<D> key) {
        return textEditor.getUserData(key);
    }

    public <D> void putUserData(@NotNull Key<D> key, D value) {
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

    @NotNull
    public TextEditor getTextEditor() {
        return textEditor;
        //return FailsafeUtil.get(textEditor);
    }

    public Project getProject() {
        return project;
    }

    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return getTextEditor().getStructureViewBuilder();
    }

    public void dispose() {
        super.dispose();
        virtualFile = null;
        project = null;
        //textEditor = null;
    }
}
