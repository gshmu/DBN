package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.ui.ColoredTableCellRenderer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DBNColoredTableCellRenderer extends ColoredTableCellRenderer {
    @Override
    protected final void customizeCellRenderer(JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
        DBNTable dbnTable = (DBNTable) table;
        Failsafe.guarded(() -> customizeCellRenderer(dbnTable, value, selected, hasFocus, row, column));
    }

    protected abstract void customizeCellRenderer(DBNTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column);
}
