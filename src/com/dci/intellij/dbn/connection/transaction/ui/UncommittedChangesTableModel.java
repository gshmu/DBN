package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.transaction.UncommittedChange;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.project.Project;

import javax.swing.event.TableModelListener;

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
        UncommittedChangeBundle uncommittedChanges = getConnectionHandler().getDataChanges();
        return uncommittedChanges == null ? 0 : uncommittedChanges.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "File" :
            columnIndex == 1 ? "Details" : null ;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return UncommittedChange.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return getConnectionHandler().getDataChanges().getChanges().get(rowIndex);
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
