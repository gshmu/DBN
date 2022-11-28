package com.dci.intellij.dbn.diagnostics.ui.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MetadataDiagnosticsTableModel extends AbstractDiagnosticsTableModel<String> {
    private final ConnectionRef connection;

    private static final String[] COLUMN_NAMES = new String[] {
            "Identifier",
            "Invocations",
            "Failures",
            "Timeouts",
            "Best query (ms)",
            "Worst query (ms)",
            "Average query (ms)",
            "Total query (ms)",
            "Best load (ms)",
            "Worst load (ms)",
            "Average load (ms)",
            "Total load (ms)",
            "Fetch block size"};

    public MetadataDiagnosticsTableModel(ConnectionHandler connection) {
        super(connection.getProject());
        this.connection = connection.ref();
    }

    @NotNull
    @Override
    protected String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @NotNull
    @Override
    protected DiagnosticBundle<String> resolveDiagnostics() {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(getProject());
        return diagnosticsManager.getMetadataInterfaceDiagnostics(connection.getConnectionId());
    }

    @Override
    public Object getValue(DiagnosticEntry<String> entry, int column) {
        DiagnosticEntry<String> query = entry.getDetail("QUERY");
        DiagnosticEntry<String> load = entry.getDetail("LOAD");
        switch (column) {
            case 0: return query.getIdentifier();
            case 1: return query.getInvocations();
            case 2: return query.getFailures();
            case 3: return query.getTimeouts();
            case 4: return query.getBest();
            case 5: return query.getWorst();
            case 6: return query.getAverage();
            case 7: return query.getTotal();
            case 8: return load.getBest();
            case 9: return load.getWorst();
            case 10: return load.getAverage();
            case 11: return load.getTotal();
            case 12: return entry.getDetail("FETCH_BLOCK").getAverage();
        }
        return "";
    }

    @Override
    public String getPresentableValue(DiagnosticEntry<String> entry, int column) {
        return getValue(entry, column).toString();
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @NotNull
    public Project getProject() {
        return getConnection().getProject();
    }
}
