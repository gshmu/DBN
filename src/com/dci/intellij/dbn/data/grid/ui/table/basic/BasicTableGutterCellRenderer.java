package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.DBNTableGutterRendererBase;

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
                Colors.getTableSelectionBackground(cellHasFocus) :
                isCaretRow ?
                        Colors.getTableCaretRowColor() :
                        Colors.getPanelBackground());
        textLabel.setForeground(isSelected ?
                Colors.getTableSelectionForeground(cellHasFocus) :
                Colors.getTableLineNumberColor());
    }
}
