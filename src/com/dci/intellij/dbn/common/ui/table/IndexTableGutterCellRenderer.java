package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.Colors;
import com.intellij.util.ui.UIUtil;

import javax.swing.JList;

public class IndexTableGutterCellRenderer extends DBNTableGutterRendererBase {


    @Override
    protected void adjustListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DBNTableGutter tableGutter = (DBNTableGutter) list;
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        mainPanel.setBackground(isSelected ?
                Colors.tableSelectionBackgroundColor(true) :
                isCaretRow ?
                        Colors.tableCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ?
                Colors.tableSelectionForegroundColor(cellHasFocus) :
                Colors.tableLineNumberColor());
    }
}
