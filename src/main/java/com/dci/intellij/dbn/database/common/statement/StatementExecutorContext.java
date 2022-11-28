package com.dci.intellij.dbn.database.common.statement;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class StatementExecutorContext {
    private final DiagnosticBundle<String> diagnostics;
    private final String identifier;
    private final int timeout;
    private final DBNConnection connection;

    private DBNStatement statement;

    public StatementExecutorContext(DBNConnection connection, String identifier, int timeout) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(connection.getProject());

        this.connection = connection;
        this.diagnostics =  diagnosticsManager.getMetadataInterfaceDiagnostics(connection.getId());
        this.identifier = identifier;
        this.timeout = timeout;
    }

    public DiagnosticEntry<String> log(String qualifier, boolean failure, boolean timeout, long value) {
        return diagnostics.log(identifier, qualifier, failure, timeout, value);
    }
}
