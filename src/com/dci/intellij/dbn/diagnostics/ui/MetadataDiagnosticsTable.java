package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MetadataDiagnosticsTable extends DBNTable<MetadataDiagnosticsTableModel> {

    MetadataDiagnosticsTable(@NotNull DBNComponent parent, MetadataDiagnosticsTableModel model) {
        super(parent, model, true);
        setDefaultRenderer(DiagnosticEntry.class, new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);
        adjustRowHeight(2);
        //setRowSorter(new TableRowSorter());
        accommodateColumnsSize();
    }

    private static class CellRenderer extends DBNColoredTableCellRenderer {
        @Override
        protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            DiagnosticEntry transaction = (DiagnosticEntry) value;
            switch (column) {
                case 0: append(transaction.getIdentifier(), SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
                case 1: append(transaction.getInvocationCount() + "", SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
                case 2: append(transaction.getFailureCount() + "", SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
                case 3: append(transaction.getTimeoutCount() + "", SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
                case 4: append(transaction.getAverageExecutionTime() + "", SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
                case 5: append(transaction.getTotalExecutionTime() + "", SimpleTextAttributes.REGULAR_ATTRIBUTES); break;
            }
            setBorder(Borders.TEXT_FIELD_BORDER);
        }
    }
}
