package com.dci.intellij.dbn.diagnostics.ui.model;

import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.dci.intellij.dbn.diagnostics.ui.DiagnosticsTableModel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ParserDiagnosticsTableModel extends DiagnosticsTableModel {
    private static final String[] COLUMN_NAMES = new String[] {
            "Identifier",
            "Invocations",
            "Average Execution Time",
            "Total Execution Time"};

    public ParserDiagnosticsTableModel(Project project) {
        super(project);
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
        return diagnosticsManager.getFileParserDiagnostics();
    }

    @Override
    protected Comparable getColumnValue(DiagnosticEntry entry, int column) {
        switch (column) {
            case 0: return entry.getIdentifier();
            case 1: return entry.getInvocationCount();
            case 2: return entry.getAverageExecutionTime();
            case 3: return entry.getTotalExecutionTime();
        }
        return "";
    }


    @Override
    public void dispose() {
    }
}
