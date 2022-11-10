package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.Strings;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.border.LineBorder;

public class DatabaseFilesTableCellRenderer extends DBNColoredTableCellRenderer {
    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        String stringValue = (String) value;
        if (Strings.isNotEmpty(stringValue)) {
            append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        setBorder(new LineBorder(Colors.getTableBackground()));
    }
}
