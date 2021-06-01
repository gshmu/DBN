package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TableModelListener;

public class MetadataDiagnosticsTableModel implements DBNTableModel, Disposable {
    private final ConnectionHandlerRef connectionHandler;
    private final DiagnosticBundle diagnostics;

    public MetadataDiagnosticsTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler.getRef();
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(getProject());
        diagnostics = diagnosticsManager.getDiagnostics(connectionHandler.getConnectionId(), DiagnosticType.METADATA_LOAD);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public int getRowCount() {
        return diagnostics.getEntries().size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Identifier" :
            columnIndex == 1 ? "Invocations" :
            columnIndex == 2 ? "Failures" :
            columnIndex == 3 ? "Timeouts" :
            columnIndex == 4 ? "Average Execution Time" :
            columnIndex == 5 ? "Total Execution Time" :
                    null ;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return DiagnosticEntry.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return diagnostics.getEntries().get(rowIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

    @Override
    public void addTableModelListener(TableModelListener l) {}

    @Override
    public void removeTableModelListener(TableModelListener l) {}

    @Override
    public void dispose() {
    }
}
