package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserDetailsForm;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserForm;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SessionBrowser extends UserDataHolderBase implements FileEditor, Disposable, ConnectionProvider, DataProviderSupplier {
    private DBSessionBrowserVirtualFile sessionBrowserFile;
    private SessionBrowserForm editorForm;
    private boolean preventLoading = false;
    private boolean loading;
    private Timer refreshTimer;
    private FileEditorState cachedState;

    public SessionBrowser(DBSessionBrowserVirtualFile sessionBrowserFile) {
        this.sessionBrowserFile = sessionBrowserFile;
        editorForm = new SessionBrowserForm(this);
        Disposer.register(this, editorForm);
        loadSessions(true);
    }

    @NotNull
    public SessionBrowserTable getEditorTable() {
        return getEditorForm().getEditorTable();
    }

    @NotNull
    public SessionBrowserForm getEditorForm() {
        return FailsafeUtil.get(editorForm);
    }

    public void showSearchHeader() {
        getEditorForm().showSearchHeader();
    }

    @Nullable
    public SessionBrowserModel getTableModel() {
        return getEditorTable().getModel();
    }

    public SessionBrowserSettings getSettings() {
        return OperationSettings.getInstance(getProject()).getSessionBrowserSettings();
    }

    public boolean isPreventLoading(boolean force) {
        if (force) return false;
        SessionBrowserTable editorTable = getEditorTable();
        return preventLoading || editorTable.getSelectedRowCount() > 1;
    }

    public void loadSessions(final boolean force) {
        if (shouldLoad(force)) {
            new ConnectionAction("loading the sessions", this, new TaskInstructions("Loading sessions", TaskInstruction.START_IN_BACKGROUND)) {
                @Override
                protected void execute() {
                    if (shouldLoad(force)) {
                        try {
                            setLoading(true);
                            SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
                            SessionBrowserModel model = sessionBrowserManager.loadSessions(sessionBrowserFile);
                            replaceModel(model);
                        } finally {
                            EventUtil.notify(getProject(), SessionBrowserLoadListener.TOPIC).sessionsLoaded(sessionBrowserFile);
                            setLoading(false);
                        }
                    }
                }

                @Override
                protected void cancel() {
                    super.cancel();
                    setLoading(false);
                    setRefreshInterval(0);
                }
            }.start();
        }
    }

    boolean shouldLoad(boolean force) {
        return !loading && !isPreventLoading(force);
    }

    private void replaceModel(final SessionBrowserModel newModel) {
        if (newModel != null) {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    SessionBrowserTable editorTable = getEditorTable();
                    SessionBrowserModel oldModel = editorTable.getModel();
                    SessionBrowserState state = oldModel.getState();
                    newModel.setState(state);
                    editorTable.setModel(newModel);
                    refreshTable();
                    DisposerUtil.dispose(oldModel);
                }
            }.start();
        }
    }

    public void clearFilter() {
        SessionBrowserTable editorTable = getEditorTable();
        SessionBrowserFilterState filter = editorTable.getModel().getFilter();
        if (filter != null) {
            filter.clear();
            refreshTable();
        }
    }

    public void refreshTable() {
        SessionBrowserTable editorTable = getEditorTable();
        editorTable.revalidate();
        editorTable.repaint();
        editorTable.accommodateColumnsSize();
        editorTable.restoreSelection();
    }

    public void refreshLoadTimestamp() {
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
        loadSessions(true);
    }

    private void interruptSessions(SessionInterruptionType type) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        SessionBrowserTable editorTable = getEditorTable();
        int[] selectedRows = editorTable.getSelectedRows();
        Map<Object, Object> sessionIds = new HashMap<Object, Object>();
        for (int selectedRow : selectedRows) {
            SessionBrowserModelRow row = editorTable.getModel().getRowAtIndex(selectedRow);
            Object sessionId = row.getSessionId();
            Object serialNumber = row.getSerialNumber();
            sessionIds.put(sessionId, serialNumber);
        }

        sessionBrowserManager.interruptSessions(this, sessionIds, type);
        loadSessions(true);
    }


    public DBSessionBrowserVirtualFile getDatabaseFile() {
        return sessionBrowserFile;
    }

    public Project getProject() {
        return FailsafeUtil.get(sessionBrowserFile.getProject());
    }

    @NotNull
    public JComponent getComponent() {
        return disposed ? new JPanel() : editorForm.getComponent();
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return isDisposed() ? null : getEditorTable();
    }

    @NonNls
    @NotNull
    public String getName() {
        return "Data";
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        if (!isDisposed()) {
            SessionBrowserTable editorTable = getEditorTable();
            SessionBrowserModel model = editorTable.getModel();
            cachedState = model.getState().clone();
        }
        return cachedState;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof SessionBrowserState) {
            SessionBrowserTable editorTable = getEditorTable();
            SessionBrowserModel model = editorTable.getModel();
            SessionBrowserState sessionBrowserState = (SessionBrowserState) fileEditorState;
            model.setState(sessionBrowserState);
            refreshTable();
            startRefreshTimer((sessionBrowserState).getRefreshInterval());
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

    public void setPreventLoading(boolean preventLoading) {
        this.preventLoading = preventLoading;
    }

    public boolean isLoading() {
        return loading;
    }

    protected void setLoading(boolean loading) {
        if (this.loading != loading) {
            this.loading = loading;

            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    if (editorForm != null) {
                        if (SessionBrowser.this.loading)
                            editorForm.showLoadingHint(); else
                            editorForm.hideLoadingHint();
                    }

                    SessionBrowserTable editorTable = getEditorTable();
                    editorTable.setLoading(SessionBrowser.this.loading);
                    editorTable.revalidate();
                    editorTable.repaint();
                }
            }.start();
        }

    }

    public int getRowCount() {
        return getEditorTable().getRowCount();
    }

    public void setRefreshInterval(int refreshInterval) {
        SessionBrowserModel tableModel = getTableModel();
        if (tableModel != null) {
            SessionBrowserState state = tableModel.getState();
            if (state.getRefreshInterval() != refreshInterval) {
                state.setRefreshInterval(refreshInterval);
                stopRefreshTimer();
                startRefreshTimer(refreshInterval);
            }
        }
    }

    private void startRefreshTimer(int refreshInterval) {
        if (refreshTimer == null && refreshInterval > 0) {
            refreshTimer = new Timer("DBN - Session Browser (refresh timer)");
            int period = refreshInterval * 1000;
            refreshTimer.schedule(new RefreshTask(), period, period);
        }
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    public String getModelError() {
        SessionBrowserModel tableModel = getTableModel();
        return tableModel == null ? null : tableModel.getLoadError();
    }

    public int getRefreshInterval() {
        SessionBrowserModel tableModel = getTableModel();
        return tableModel == null ? 0 : tableModel.getState().getRefreshInterval();
    }

    @Nullable
    public Object getSelectedSessionId() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable.getSelectedRowCount() == 1) {
            int rowIndex = editorTable.getSelectedRow();
            SessionBrowserModelRow rowAtIndex = editorTable.getModel().getRowAtIndex(rowIndex);
            return rowAtIndex.getSessionId();
        }
        return null;
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            loadSessions(false);
        }
    }

    public void updateDetails() {
        if (editorForm != null) {
            SessionBrowserTable editorTable = editorForm.getEditorTable();
            SessionBrowserDetailsForm detailsForm = editorForm.getDetailsForm();
            if (editorTable.getSelectedRowCount() == 1) {
                SessionBrowserModelRow selectedRow = editorTable.getModel().getRowAtIndex(editorTable.getSelectedRow());
                detailsForm.update(selectedRow);
            } else {
                detailsForm.update(null);
            }
        }
    }

    @NotNull
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
            return null;
        }
    };

    @Nullable
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

    @Override
    public void checkDisposed() {
        if (disposed) throw AlreadyDisposedException.INSTANCE;
    }
}

