package com.dci.intellij.dbn.diagnostics.ui.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MetadataDiagnosticsTableModel extends AbstractDiagnosticsTableModel {
    private final ConnectionHandlerRef connectionHandler;

    private static final String[] COLUMN_NAMES = new String[] {
            "Identifier",
            "Invocations",
            "Failures",
            "Timeouts",
            "Average Execution Time (ms)",
            "Total Execution Time (ms)"};

    public MetadataDiagnosticsTableModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler.getRef();
    }

    @NotNull
    @Override
    protected String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @NotNull
    @Override
    protected DiagnosticBundle resolveDiagnostics() {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(getProject());
        return diagnosticsManager.getMetadataInterfaceDiagnostics(connectionHandler.getConnectionId());
    }

    @Override
    public Object getValue(DiagnosticEntry entry, int column) {
        switch (column) {
            case 0: return entry.getIdentifier();
            case 1: return entry.getInvocationCount();
            case 2: return entry.getFailureCount();
            case 3: return entry.getTimeoutCount();
            case 4: return entry.getAverageExecutionTime();
            case 5: return entry.getTotalExecutionTime();
        }
        return "";
    }

    @Override
    public String getPresentableValue(DiagnosticEntry entry, int column) {
        switch (column) {
            case 0: return entry.getIdentifier();
            case 1: return Long.toString(entry.getInvocationCount());
            case 2: return Long.toString(entry.getFailureCount());
            case 3: return Long.toString(entry.getTimeoutCount());
            case 4: return Long.toString(entry.getAverageExecutionTime());
            case 5: return Long.toString(entry.getTotalExecutionTime());
        }
        return "";
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public void dispose() {
    }
}
