package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticEntry;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DiagnosticsTable<T extends DiagnosticsTableModel> extends DBNTable<T> {
    private final Map<Integer, Comparator<DiagnosticEntry>> comparators = new HashMap<>();

    DiagnosticsTable(@NotNull DBNComponent parent, T model) {
        super(parent, model, true);
        setDefaultRenderer(DiagnosticEntry.class, new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);
        adjustRowHeight(2);
        setRowSorter(createSorter(model));
        accommodateColumnsSize();
    }

    @NotNull
    private TableRowSorter<T> createSorter(T model) {
        return new TableRowSorter<T>(model) {
            @Override
            public Comparator<?> getComparator(int column) {
                return DiagnosticsTable.this.getComparator(column);
            }

            @Override
            protected boolean useToString(int column) {
                return false;
            }
        };
    }

    private Comparator<DiagnosticEntry> getComparator(int column) {
        return comparators.computeIfAbsent(column, col -> (Comparator<DiagnosticEntry>) Comparator.comparing(o -> getModel().getColumnValue((DiagnosticEntry) o, col)));
    }

    private class CellRenderer extends DBNColoredTableCellRenderer {
        @Override
        protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            DiagnosticEntry entry = (DiagnosticEntry) value;
            Object columnValue = getModel().getColumnValue(entry, column);
            append(columnValue.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setBorder(Borders.TEXT_FIELD_BORDER);
        }
    }
}
