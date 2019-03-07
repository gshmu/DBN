package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.transaction.PendingTransaction;
import com.intellij.ui.SimpleTextAttributes;

public class PendingTransactionsTableCellRenderer extends DBNColoredTableCellRenderer {
    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        PendingTransaction change = (PendingTransaction) value;
        if (column == 0) {
            setIcon(change.getIcon());
            append(change.getDisplayFilePath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else if (column == 1) {
            append(change.getChangesCount() + " uncommitted changes", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }

    }
}
