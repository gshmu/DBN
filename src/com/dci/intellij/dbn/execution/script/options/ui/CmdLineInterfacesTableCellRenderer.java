package com.dci.intellij.dbn.execution.script.options.ui;

import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.ui.SimpleTextAttributes;

public class CmdLineInterfacesTableCellRenderer extends DBNColoredTableCellRenderer {
    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        if (value instanceof DatabaseType) {
            DatabaseType databaseType = (DatabaseType) value;
            setIcon(databaseType.getIcon());
            append(databaseType.getName());
        } if (value instanceof String) {
            String stringValue = (String) value;
            if (StringUtil.isNotEmpty(stringValue)) {
                append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }
}
