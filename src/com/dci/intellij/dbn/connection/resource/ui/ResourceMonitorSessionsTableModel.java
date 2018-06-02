package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelListener;
import java.util.List;

public class ResourceMonitorSessionsTableModel extends DisposableBase implements DBNTableModel {
    private ConnectionHandlerRef connectionHandlerRef;
    private List<DatabaseSession> sessions;

    public ResourceMonitorSessionsTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        sessions = connectionHandler.getSessionBundle().getSessions();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public List<DatabaseSession> getSessions() {
        return FailsafeUtil.get(sessions);
    }

    public int getRowCount() {
        return getSessions().size();
    }

    public int getColumnCount() {
        return 4;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Session";
            case 1: return "Status";
            case 2: return "Last access";
            case 3: return "Size / Peak";
        }
        return null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return DatabaseSession.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return getSession(rowIndex);
    }

    public DatabaseSession getSession(int rowIndex) {
        return sessions.get(rowIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    public void addTableModelListener(TableModelListener l) {}
    public void removeTableModelListener(TableModelListener l) {}

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            sessions = null;
        }

    }

}
