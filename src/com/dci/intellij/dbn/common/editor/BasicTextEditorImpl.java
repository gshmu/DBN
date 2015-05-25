package com.dci.intellij.dbn.common.editor;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public abstract class BasicTextEditorImpl<T extends VirtualFile> implements BasicTextEditor<T>{
    protected TextEditor textEditor;
    private T virtualFile;
    private String name;
    private EditorProviderId editorProviderId;
    private BasicTextEditorState cachedState;

    public BasicTextEditorImpl(Project project, T virtualFile, String name, EditorProviderId editorProviderId) {
        this.name = name;
        this.virtualFile = virtualFile;
        this.editorProviderId = editorProviderId;
        textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, virtualFile);
        Disposer.register(this, textEditor);
    }

    public T getVirtualFile() {
        return virtualFile;
    }

    public <T> T getUserData(@NotNull Key<T> key) {
        return getTextEditor().getUserData(key);
    }

    public <T> void putUserData(@NotNull Key<T> key, T value) {
        getTextEditor().putUserData(key, value);
    }

    public boolean isModified() {
        return getTextEditor().isModified();
    }

    public boolean isValid() {
        return textEditor != null && textEditor.isValid();
    }

    public void selectNotify() {
        getTextEditor().selectNotify();
    }

    public void deselectNotify() {
        getTextEditor().deselectNotify();
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        getTextEditor().addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        getTextEditor().removePropertyChangeListener(listener);
    }

    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return getTextEditor().getBackgroundHighlighter();
    }

    public FileEditorLocation getCurrentLocation() {
        return getTextEditor().getCurrentLocation();
    }

    @NotNull
    public Editor getEditor() {
        return getTextEditor().getEditor();
    }

    public boolean canNavigateTo(@NotNull final Navigatable navigatable) {
        return getTextEditor().canNavigateTo(navigatable);
    }

    public void navigateTo(@NotNull final Navigatable navigatable) {
        getTextEditor().navigateTo(navigatable);
    }

    @NotNull
    public JComponent getComponent() {
        return getTextEditor().getComponent();
    }

    @Override
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return getTextEditor().getPreferredFocusedComponent();
    }

    protected BasicTextEditorState createEditorState() {
        return new BasicTextEditorState();
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        if (!isDisposed()) {
            cachedState = createEditorState();
            cachedState.loadFromEditor(level, getTextEditor());
        }
        return cachedState;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof BasicTextEditorState) {
            BasicTextEditorState state = (BasicTextEditorState) fileEditorState;
            state.applyToEditor(getTextEditor());
        }
    }

    @NonNls
    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public TextEditor getTextEditor() {
        return FailsafeUtil.get(textEditor);
    }

    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return getTextEditor().getStructureViewBuilder();
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        virtualFile = null;
        textEditor = null;
    }
}
