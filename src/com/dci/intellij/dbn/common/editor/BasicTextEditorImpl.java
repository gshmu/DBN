package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.action.DataProviders;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.vfs.DatabaseOpenFileDescriptor;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataProvider;
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

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

public abstract class BasicTextEditorImpl<T extends VirtualFile> extends StatefulDisposable.Base implements BasicTextEditor<T>, StatefulDisposable, DataProvider {
    protected TextEditor textEditor;
    private final WeakRef<T> virtualFile;
    private final ProjectRef project;
    private final String name;
    private final EditorProviderId editorProviderId;
    private BasicTextEditorState cachedState;

    public BasicTextEditorImpl(Project project, T virtualFile, String name, EditorProviderId editorProviderId) {
        this.project = ProjectRef.of(project);
        this.name = name;
        this.virtualFile = WeakRef.of(virtualFile);
        this.editorProviderId = editorProviderId;

        TextEditorProvider textEditorProvider = TextEditorProvider.getInstance();
        textEditor = (TextEditor) textEditorProvider.createEditor(project, virtualFile);
        DataProviders.register(textEditor.getComponent(), this);

        Disposer.register(this, textEditor);
    }

    @Override
    @NotNull
    public T getVirtualFile() {
        return virtualFile.ensure();
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return virtualFile.get();
    }

    @Override
    public <D> D getUserData(@NotNull Key<D> key) {
        return getTextEditor().getUserData(key);
    }

    @Override
    public <D> void putUserData(@NotNull Key<D> key, D value) {
        getTextEditor().putUserData(key, value);
    }

    @Override
    public boolean isModified() {
        return getTextEditor().isModified();
    }

    @Override
    public boolean isValid() {
        return !isDisposed() && getTextEditor().isValid();
    }

    @Override
    public void selectNotify() {
        getTextEditor().selectNotify();
    }

    @Override
    public void deselectNotify() {
        getTextEditor().deselectNotify();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        getTextEditor().addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        getTextEditor().removePropertyChangeListener(listener);
    }

    @Override
    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return getTextEditor().getBackgroundHighlighter();
    }

    @Override
    public FileEditorLocation getCurrentLocation() {
        return getTextEditor().getCurrentLocation();
    }

    @Override
    @NotNull
    public Editor getEditor() {
        return getTextEditor().getEditor();
    }

    @Override
    public boolean canNavigateTo(@NotNull final Navigatable navigatable) {
        return navigatable instanceof DatabaseOpenFileDescriptor && getTextEditor().canNavigateTo(navigatable);
    }

    @Override
    public void navigateTo(@NotNull final Navigatable navigatable) {
        getTextEditor().navigateTo(navigatable);
    }

    @Override
    @NotNull
    public JComponent getComponent() {
        return getTextEditor().getComponent();
    }

    @Override
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return isDisposed() ? null : getTextEditor().getPreferredFocusedComponent();
    }

    protected BasicTextEditorState createEditorState() {
        return new BasicTextEditorState();
    }

    @Override
    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        if (!isDisposed()) {
            cachedState = createEditorState();
            cachedState.loadFromEditor(level, getTextEditor());
        }
        return cachedState;
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof BasicTextEditorState) {
            BasicTextEditorState state = (BasicTextEditorState) fileEditorState;
            state.applyToEditor(getTextEditor());
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
        return Failsafe.nn(textEditor);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return getTextEditor().getStructureViewBuilder();
    }

    @Override
    public String toString() {
        T virtualFile = this.virtualFile.get();
        return virtualFile == null ? super.toString() : virtualFile.getPath();
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        return null;
    }

    @Override
    public void disposeInner() {
        // TODO cleanup - happens as part of text editor disposal
        // EditorUtil.releaseEditor(textEditor.getEditor());
    }

}
