package com.dci.intellij.dbn.editor.session;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.editor.data.state.DatasetEditorState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;

public class SessionBrowser extends UserDataHolderBase implements FileEditor, Disposable, ConnectionProvider {
    private DBSessionBrowserVirtualFile sessionBrowserFile;
    private SessionBrowserForm editorForm;
    private boolean isLoading;

    private DatasetEditorState editorState = new DatasetEditorState();

    public SessionBrowser(DBSessionBrowserVirtualFile sessionBrowserFile) {
        this.sessionBrowserFile = sessionBrowserFile;
        editorForm = new SessionBrowserForm(this, sessionBrowserFile.getModel());
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

    public void reload() {
        new BackgroundTask(getProject(), "Reloading sessions", false) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                final SessionBrowserModel sessionBrowserModel = sessionBrowserFile.load();
                if (sessionBrowserModel != null) {
                    new SimpleLaterInvocator() {
                        @Override
                        protected void execute() {
                            SessionBrowserTable editorTable = getEditorTable();
                            if (editorTable != null) {
                                editorTable.setModel(sessionBrowserModel);
                                refreshTable();
                            }
                        }
                    }.start();
                }
            }
        }.start();
    }

    public void refreshTable() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            editorTable.revalidate();
            editorTable.repaint();
            editorTable.accommodateColumnsSize();
        }
    }

    public void killSelectedSessions(boolean immediate) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            int[] selectedRows = editorTable.getSelectedRows();
            Map<Object, Object> sessionIds = new HashMap<Object, Object>();
            for (int selectedRow : selectedRows) {
                SessionBrowserModelRow row = editorTable.getModel().getRowAtIndex(selectedRow);
                Object sessionId = row.getCellValue("SESSION_ID");
                Object serialNumber = row.getCellValue("SERIAL_NUMBER");
                sessionIds.put(sessionId, serialNumber);
            }

            sessionBrowserManager.killSessions(this, sessionIds, immediate);
        }
    }


    public DBSessionBrowserVirtualFile getDatabaseFile() {
        return sessionBrowserFile;
    }

    public Project getProject() {
        return sessionBrowserFile.getProject();
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
        return sessionBrowserFile.getConnectionHandler();
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
                return getProject();
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
