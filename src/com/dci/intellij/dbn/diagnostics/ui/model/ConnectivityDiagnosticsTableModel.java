package com.dci.intellij.dbn.diagnostics.ui.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectivityDiagnosticsTableModel extends AbstractDiagnosticsTableModel<SessionId> {
    private final ConnectionHandlerRef connection;

    private static final String[] COLUMN_NAMES = new String[] {
            "Session",
            "Attempts",
            "Failures",
            "Timeouts",
            "Average Time (ms)",
            "Total Time (ms)"};

    public ConnectivityDiagnosticsTableModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connection = connectionHandler.getRef();
    }

    @NotNull
    @Override
    protected String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @NotNull
    @Override
    protected DiagnosticBundle<SessionId> resolveDiagnostics() {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(getProject());
        return diagnosticsManager.getConnectivityDiagnostics(connection.getConnectionId());
    }

    @Override
    public Object getValue(DiagnosticEntry<SessionId> entry, int column) {
        switch (column) {
            case 0: return getSession(entry.getIdentifier());
            case 1: return entry.getInvocationCount();
            case 2: return entry.getFailureCount();
            case 3: return entry.getTimeoutCount();
            case 4: return entry.getAverageExecutionTime();
            case 5: return entry.getTotalExecutionTime();
        }
        return "";
    }

    @NotNull
    private DatabaseSession getSession(SessionId sessionId) {
        return getConnection().getSessionBundle().getSession(sessionId);
    }

    @Override
    public String getPresentableValue(DiagnosticEntry<SessionId> entry, int column) {
        switch (column) {
            case 0: return getSession(entry.getIdentifier()).getName();
            case 1: return Long.toString(entry.getInvocationCount());
            case 2: return Long.toString(entry.getFailureCount());
            case 3: return Long.toString(entry.getTimeoutCount());
            case 4: return Long.toString(entry.getAverageExecutionTime());
            case 5: return Long.toString(entry.getTotalExecutionTime());
        }
        return "";
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnection().getProject();
    }
}
