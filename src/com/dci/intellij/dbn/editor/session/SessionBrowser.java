package com.dci.intellij.dbn.editor.session;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
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
    private Timer refreshTimer;

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

    public SessionBrowserSettings getSettings() {
        return OperationSettings.getInstance(getProject()).getSessionBrowserSettings();
    }

    public void reload() {
        new BackgroundTask(getProject(), "Reloading sessions", false) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                progressIndicator.setIndeterminate(true);
                editorForm.showLoadingHint();
                try {
                    final SessionBrowserModel sessionBrowserModel = sessionBrowserFile.load();
                    updateModel(sessionBrowserModel);
                } finally {
                    editorForm.hideLoadingHint();
                }
            }

            private void updateModel(final SessionBrowserModel sessionBrowserModel) {
                if (sessionBrowserModel != null) {
                    new SimpleLaterInvocator() {
                        @Override
                        protected void execute() {
                            SessionBrowserTable editorTable = getEditorTable();
                            if (editorTable != null) {
                                editorTable.setModel(sessionBrowserModel);
                                refreshTable();
                            }
                            EventManager.notify(getProject(), SessionBrowserLoadListener.TOPIC).sessionsLoaded(sessionBrowserFile);
                        }
                    }.start();
                }
            }
        }.start();
    }

    public void clearFilter() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            SessionBrowserFilterState filter = editorTable.getModel().getFilter();
            if (filter != null) {
                filter.clear();
                refreshTable();
            }
        }
    }

    public void refreshTable() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            editorTable.revalidate();
            editorTable.repaint();
            editorTable.accommodateColumnsSize();
        }
    }

    public void refreshLoadTimestamp() {
        SessionBrowserForm editorForm = getEditorForm();
        if (editorForm != null) {
            editorForm.refreshLoadTimestamp();
        }
    }

    public void disconnectSelectedSessions() {
        interruptSessions(SessionInterruptionType.DISCONNECT);
    }

    public void killSelectedSessions() {
        interruptSessions(SessionInterruptionType.KILL);
    }

    public void interruptSession(Object sessionId, Object serialNumber, SessionInterruptionType type) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        Map<Object, Object> sessionIds = new HashMap<Object, Object>();
        sessionIds.put(sessionId, serialNumber);
        sessionBrowserManager.interruptSessions(this, sessionIds, type);
    }

    private void interruptSessions(SessionInterruptionType type) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            int[] selectedRows = editorTable.getSelectedRows();
            Map<Object, Object> sessionIds = new HashMap<Object, Object>();
            for (int selectedRow : selectedRows) {
                SessionBrowserModelRow row = editorTable.getModel().getRowAtIndex(selectedRow);
                Object sessionId = row.getSessionId();
                Object serialNumber = row.getSerialNumber();
                sessionIds.put(sessionId, serialNumber);
            }

            sessionBrowserManager.interruptSessions(this, sessionIds, type);
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
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            SessionBrowserModel model = editorTable.getModel();
            return model.getState().clone();
        }
        return SessionBrowserState.VOID;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof SessionBrowserState) {
            SessionBrowserTable editorTable = getEditorTable();
            if (editorTable != null) {
                SessionBrowserModel model = editorTable.getModel();
                model.setState((SessionBrowserState) fileEditorState);
                refreshTable();
            }
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

    public void setRefreshInterval(int refreshInterval) {
        SessionBrowserModel tableModel = getTableModel();
        if (tableModel != null) {
            SessionBrowserState state = tableModel.getState();
            if (state.getRefreshInterval() != refreshInterval) {
                state.setRefreshInterval(refreshInterval);
                stopRefreshTimer();
                if (refreshInterval > 0) {
                    refreshTimer = new Timer("DBN Session Browser refresher");
                    refreshTimer.schedule(new RefreshTask(), 0, refreshInterval * 1000);
                }
            }
        }
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    public int getRefreshInterval() {
        SessionBrowserModel tableModel = getTableModel();
        return tableModel == null ? 0 : tableModel.getState().getRefreshInterval();
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            reload();
        }
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
            stopRefreshTimer();
            editorForm = null;
        }
    }
}

