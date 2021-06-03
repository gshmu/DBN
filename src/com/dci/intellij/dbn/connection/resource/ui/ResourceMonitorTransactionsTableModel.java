package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.ui.table.DBNReadonlyTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.PendingTransaction;
import com.dci.intellij.dbn.connection.transaction.PendingTransactionBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceMonitorTransactionsTableModel implements DBNReadonlyTableModel, Disposable {
    private final ConnectionHandlerRef connectionHandler;
    private DBNConnection connection;

    public ResourceMonitorTransactionsTableModel(ConnectionHandler connectionHandler, @Nullable DBNConnection connection) {
        this.connectionHandler = connectionHandler.getRef();
        this.connection = connection;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    public DBNConnection getConnection() {
        return connection;
    }

    @Override
    public int getRowCount() {
        PendingTransactionBundle dataChanges = connection == null ? null : connection.getDataChanges();
        return dataChanges == null ? 0 : dataChanges.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Source" :
            columnIndex == 1 ? "Details" : null ;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return PendingTransaction.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PendingTransactionBundle dataChanges = connection == null ? null : connection.getDataChanges();
        if (dataChanges != null) {
            return dataChanges.getEntries().get(rowIndex);
        }
        return null;
    }

    @Override
    public void dispose() {
        connection = null;
    }
}
