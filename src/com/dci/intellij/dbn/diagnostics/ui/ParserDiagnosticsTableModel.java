package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.table.DBNReadonlyTableModel;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsDeltaResult;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsEntry;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

public class ParserDiagnosticsTableModel implements DBNReadonlyTableModel<ParserDiagnosticsEntry>, Disposable {
    private final ParserDiagnosticsDeltaResult deltaResult;

    public ParserDiagnosticsTableModel(ParserDiagnosticsDeltaResult deltaResult) {
        this.deltaResult = deltaResult;
    }

    @NotNull
    protected String[] getColumnNames() {
        return new String[] {"File", "Original Error Count", "Current Error Count", "Transition"};
    }

    @Override
    public final int getRowCount() {
        return deltaResult.getEntries().size();
    }

    @Override
    public final int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public final String getColumnName(int columnIndex) {
        return getColumnNames()[columnIndex];
    }

    @Override
    public final Class<?> getColumnClass(int columnIndex) {
        return DiagnosticEntry.class;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        return deltaResult.getEntries().get(rowIndex);
    }

    @Override
    public Object getValue(ParserDiagnosticsEntry row, int column) {
        switch (column) {
            case 0: return row.getFilePath();
            case 1: return row.getOldErrorCount();
            case 2: return row.getNewErrorCount();
            case 3: return row.getStateTransition();
            default: return "";
        }
    }

    @Override
    public String getPresentableValue(ParserDiagnosticsEntry row, int column) {
        switch (column) {
            case 0: return row.getFilePath();
            case 1: return Integer.toString(row.getOldErrorCount());
            case 2: return Integer.toString(row.getNewErrorCount());
            case 3: return row.getStateTransition().name();
            default: return "";
        }
    }

    @Override
    public void dispose() {
    }
}
