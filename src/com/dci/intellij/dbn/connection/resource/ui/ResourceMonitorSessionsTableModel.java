package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TableModelListener;
import java.util.List;

public class ResourceMonitorSessionsTableModel extends StatefulDisposable.Base implements DBNTableModel {
    private final ConnectionHandlerRef connectionHandler;
    private final List<DatabaseSession> sessions;

    ResourceMonitorSessionsTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler.getRef();
        sessions = connectionHandler.getSessionBundle().getSessions();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public List<DatabaseSession> getSessions() {
        return Failsafe.nn(sessions);
    }

    @Override
    public int getRowCount() {
        return getSessions().size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Session";
            case 1: return "Status";
            case 2: return "Last Access";
            case 3: return "Open Connections / Peak";
            case 4: return "Open Cursors";
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return DatabaseSession.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getSession(rowIndex);
    }

    @Nullable
    public DatabaseSession getSession(int rowIndex) {
        return rowIndex == -1 ? null : sessions.get(rowIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

    @Override
    public void addTableModelListener(TableModelListener l) {}

    @Override
    public void removeTableModelListener(TableModelListener l) {}

    @Override
    protected void disposeInner() {
        nullify();
    }
}
