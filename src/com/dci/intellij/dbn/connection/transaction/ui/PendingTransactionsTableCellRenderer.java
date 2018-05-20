package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.connection.transaction.PendingTransaction;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;

public class PendingTransactionsTableCellRenderer extends ColoredTableCellRenderer{
    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        PendingTransaction change = (PendingTransaction) value;
        if (column == 0) {
            setIcon(change.getIcon());
            append(change.getDisplayFilePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else if (column == 1) {
            append(change.getChangesCount() + " uncommitted changes", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }

    }
}
