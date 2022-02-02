package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.DBNTableGutterRendererBase;
import com.intellij.util.ui.UIUtil;

import javax.swing.JList;

public class BasicTableGutterCellRenderer extends DBNTableGutterRendererBase {

    @Override
    protected void adjustListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;

        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = Failsafe.check(table) &&
                table.getCellSelectionEnabled() &&
                table.getSelectedRow() == index &&
                table.getSelectedRowCount() == 1;

        mainPanel.setBackground(isSelected ?
                Colors.tableSelectionBackgroundColor(cellHasFocus) :
                isCaretRow ?
                        Colors.tableCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ?
                Colors.tableSelectionForegroundColor(cellHasFocus) :
                Colors.tableLineNumberColor());
    }
}
