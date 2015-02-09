package com.dci.intellij.dbn.editor.session;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.editor.data.state.DatasetEditorState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserForm;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;

public class SessionBrowser extends UserDataHolderBase implements FileEditor, Disposable, ConnectionProvider {
    private DBSessionBrowserVirtualFile sessionsFile;
    private SessionBrowserForm editorForm;
    private ConnectionHandler connectionHandler;
    private Project project;
    private boolean isLoading;

    private DatasetEditorState editorState = new DatasetEditorState();

    public SessionBrowser(DBSessionBrowserVirtualFile sessionsFile, ResultSet resultSet) {
        this.project = sessionsFile.getProject();
        this.sessionsFile = sessionsFile;

        connectionHandler = sessionsFile.getConnectionHandler();
        editorForm = new SessionBrowserForm(this, resultSet);
        Disposer.register(this, editorForm);
    }

    @Nullable
    public SessionBrowserTable getEditorTable() {
        return editorForm == null ? null : editorForm.getEditorTable();
    }

    public SessionBrowserForm getEditorForm() {
        return editorForm;
    }

    public void showSearchHeader() {
        editorForm.showSearchHeader();
    }

    @Nullable
    public SessionBrowserModel getTableModel() {
        SessionBrowserTable browserTable = getEditorTable();
        return browserTable == null ? null : browserTable.getModel();
    }



    public DBSessionBrowserVirtualFile getDatabaseFile() {
        return sessionsFile;
    }

    public Project getProject() {
        return project;
    }

    @NotNull
    public JComponent getComponent() {
        return isDisposed() ? new JPanel() : editorForm.getComponent();
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return getEditorTable();
    }

    @NonNls
    @NotNull
    public String getName() {
        return "Data";
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return editorState;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof DatasetEditorState) {
            editorState = (DatasetEditorState) fileEditorState;
        }
    }

    public boolean isModified() {
        return false;
    }

    public boolean isValid() {
        return true;
    }

    public void selectNotify() {

    }

    public void deselectNotify() {

    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    protected void setLoading(boolean loading) {
        if (this.isLoading != loading) {
            this.isLoading = loading;
            SessionBrowserTable editorTable = getEditorTable();
            if (editorTable != null) {
                editorTable.setLoading(loading);
                editorTable.revalidate();
                editorTable.repaint();
            }
        }

    }

    public int getRowCount() {
        SessionBrowserTable browserTable = getEditorTable();
        return browserTable == null ? 0 : browserTable.getRowCount();
    }


    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    /*******************************************************
     *                      Listeners                      *
     *******************************************************/


    /*******************************************************
     *                   Data Provider                     *
     *******************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.SESSION_BROWSER.is(dataId)) {
                return SessionBrowser.this;
            }
            if (PlatformDataKeys.PROJECT.is(dataId)) {
                return project;
            }
            return null;
        }
    };

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            editorForm = null;
        }
    }


}
