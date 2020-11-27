package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PendingTransactionsTableModel extends StatefulDisposable.Base implements DBNTableModel {
    private final ConnectionHandlerRef connectionHandler;
    private final List<DBNConnection> connections;

    PendingTransactionsTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler.getRef();
        this.connections = connectionHandler.getConnections(
                ConnectionType.MAIN,
                ConnectionType.SESSION,
                ConnectionType.DEBUG,
                ConnectionType.DEBUGGER);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @NotNull
    public List<DBNConnection> getConnections() {
        return connections;
    }

    @NotNull
    public List<DBNConnection> getTransactionalConnections() {
        return connections.stream().filter(connection -> connection.getDataChanges() != null).collect(Collectors.toList());
    }

    @Override
    public int getRowCount() {
        return (int) getRows().count();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Session" :
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
    public PendingTransaction getValueAt(int rowIndex, int columnIndex) {
        return getRows().collect(Collectors.toList()).get(rowIndex);
    }

    private Stream<PendingTransaction> getRows() {
        return connections.stream().flatMap(connection -> {
            PendingTransactionBundle dataChanges = connection.getDataChanges();
            return dataChanges == null ? Stream.empty() : dataChanges.getEntries().stream();
        });
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

    @Override
    public void addTableModelListener(TableModelListener l) {}

    @Override
    public void removeTableModelListener(TableModelListener l) {}


    @Override
    protected void disposeInner() {
        connections.clear();
    }
}
