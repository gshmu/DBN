package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import javax.swing.JTable;
import javax.swing.border.LineBorder;

public class DatabaseFilesTableCellRenderer extends ColoredTableCellRenderer{
    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        String stringValue = (String) value;
        if (StringUtil.isNotEmpty(stringValue)) {
            append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        setBorder(new LineBorder(UIUtil.getTableBackground()));
    }
}
