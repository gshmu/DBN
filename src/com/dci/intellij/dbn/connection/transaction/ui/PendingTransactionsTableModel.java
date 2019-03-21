package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.PendingTransaction;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelListener;
import java.util.List;

public class PendingTransactionsTableModel extends DisposableBase implements DBNTableModel {
    private ConnectionHandlerRef connectionHandlerRef;
    private List<DBNConnection> connections;

    PendingTransactionsTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
        connections = connectionHandler.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.getnn();
    }

    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return Failsafe.get(connections);
    }

    @Override
    public int getRowCount() {
        int count = 0;
        for (DBNConnection connection : getConnections()) {
            PendingTransactionBundle dataChanges = connection.getDataChanges();
            count += dataChanges == null ? 0 : dataChanges.size();
        }

        return count;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Connection" :
            columnIndex == 1 ? "Source" :
            columnIndex == 2 ? "Details" : null ;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return PendingTransaction.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int count = 0;
        for (DBNConnection connection : getConnections()) {
            PendingTransactionBundle dataChanges = connection.getDataChanges();
            int size = dataChanges == null ? 0 : dataChanges.size();
            count += size;
            if (dataChanges != null && count > rowIndex) {
                return dataChanges.getEntries().get(count - size + rowIndex);
            }
        }

        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    @Override
    public void addTableModelListener(TableModelListener l) {}
    @Override
    public void removeTableModelListener(TableModelListener l) {}

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Override
    public void disposeInner() {
        super.disposeInner();
        nullify();
    }

}
