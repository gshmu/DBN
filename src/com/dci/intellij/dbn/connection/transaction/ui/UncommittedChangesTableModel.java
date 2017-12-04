package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.event.TableModelListener;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.UncommittedChange;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.project.Project;

public class UncommittedChangesTableModel extends DisposableBase implements DBNTableModel {
    private ConnectionHandlerRef connectionHandlerRef;
    private List<DBNConnection> connections;

    public UncommittedChangesTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return FailsafeUtil.get(connections);
    }

    public int getRowCount() {
        int count = 0;
        for (DBNConnection connection : getConnections()) {
            UncommittedChangeBundle dataChanges = connection.getDataChanges();
            count += dataChanges == null ? 0 : dataChanges.size();
        }

        return count;
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Connection" :
            columnIndex == 1 ? "Source" :
            columnIndex == 2 ? "Details" : null ;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return UncommittedChange.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        int count = 0;
        for (DBNConnection connection : getConnections()) {
            UncommittedChangeBundle dataChanges = connection.getDataChanges();
            int size = dataChanges == null ? 0 : dataChanges.size();
            count += size;
            if (dataChanges != null && count > rowIndex) {
                return dataChanges.getChanges().get(count - size + rowIndex);
            }
        }

        return null;
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
            connections.clear();
            connections = null;
        }

    }

}
