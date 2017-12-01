package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.event.TableModelListener;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.UncommittedChange;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.project.Project;

public class UncommittedChangesTableModel extends DisposableBase implements DBNTableModel {
    private ConnectionHandlerRef connectionHandlerRef;

    public UncommittedChangesTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    public int getRowCount() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        List<DBNConnection> connections = connectionHandler.getActiveConnections();
        int count = 0;
        for (DBNConnection connection : connections) {
            UncommittedChangeBundle dataChanges = connection.getStatusMonitor().getDataChanges();
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
        ConnectionHandler connectionHandler = getConnectionHandler();
        List<DBNConnection> connections = connectionHandler.getActiveConnections();
        int count = 0;
        for (DBNConnection connection : connections) {
            UncommittedChangeBundle dataChanges = connection.getStatusMonitor().getDataChanges();
            int size = dataChanges == null ? 0 : dataChanges.size();
            count += size;
            if (count > rowIndex) {
                return connection.getStatusMonitor().getDataChanges().getChanges().get(count - size + rowIndex);
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
        super.dispose();
    }

}
