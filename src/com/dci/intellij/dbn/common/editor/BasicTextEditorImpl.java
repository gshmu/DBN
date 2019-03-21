package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
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
    private ProjectRef projectRef;

    public BasicTextEditorImpl(Project project, T virtualFile, String name, EditorProviderId editorProviderId) {
        this.projectRef = ProjectRef.from(project);
        this.name = name;
        this.virtualFile = virtualFile;
        this.editorProviderId = editorProviderId;

        TextEditorProvider textEditorProvider = TextEditorProvider.getInstance();
        textEditor = (TextEditor) textEditorProvider.createEditor(project, virtualFile);

        Disposer.register(this, textEditor);
    }

    @Override
    @NotNull
    public T getVirtualFile() {
        return Failsafe.get(virtualFile);
    }

    @Override
    public <D> D getUserData(@NotNull Key<D> key) {
        return textEditor.getUserData(key);
    }

    @Override
    public <D> void putUserData(@NotNull Key<D> key, D value) {
        textEditor.putUserData(key, value);
    }

    @Override
    public boolean isModified() {
        return textEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return !isDisposed() && textEditor.isValid();
    }

    @Override
    public void selectNotify() {
        textEditor.selectNotify();
    }

    @Override
    public void deselectNotify() {
        textEditor.deselectNotify();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        textEditor.removePropertyChangeListener(listener);
    }

    @Override
    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return textEditor.getBackgroundHighlighter();
    }

    @Override
    public FileEditorLocation getCurrentLocation() {
        return textEditor.getCurrentLocation();
    }

    @Override
    @NotNull
    public Editor getEditor() {
        return textEditor.getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull final Navigatable navigatable) {
        return textEditor.canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull final Navigatable navigatable) {
        textEditor.navigateTo(navigatable);
    }

    @Override
    @NotNull
    public JComponent getComponent() {
        return textEditor.getComponent();
    }

    @Override
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return isDisposed() ? null : textEditor.getPreferredFocusedComponent();
    }

    protected BasicTextEditorState createEditorState() {
        return new BasicTextEditorState();
    }

    @Override
    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        if (!isDisposed()) {
            cachedState = createEditorState();
            cachedState.loadFromEditor(level, textEditor);
        }
        return cachedState;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof BasicTextEditorState) {
            BasicTextEditorState state = (BasicTextEditorState) fileEditorState;
            state.applyToEditor(textEditor);
        }
    }

    @Override
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

    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }

    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return getTextEditor().getStructureViewBuilder();
    }

    @Override
    public void disposeInner() {
        super.disposeInner();
        nullify();
    }

    @Override
    public String toString() {
        return virtualFile == null ? super.toString() : virtualFile.getPath();
    }
}
