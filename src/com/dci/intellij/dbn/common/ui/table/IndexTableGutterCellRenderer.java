package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class IndexTableGutterCellRenderer extends JPanel implements DBNTableGutterRenderer {

    private static final Border BORDER = new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), Borders.TEXT_FIELD_BORDER);
    private final JLabel textLabel;

    public IndexTableGutterCellRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(BORDER);
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setForeground(Colors.tableLineNumberColor());
        textLabel.setFont(GUIUtil.getEditorFont());
        add(textLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DBNTableGutter tableGutter = (DBNTableGutter) list;
        textLabel.setText(Integer.toString(index));
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        setBackground(isSelected ?
                Colors.tableSelectionBackgroundColor(true) :
                isCaretRow ?
                        Colors.tableCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ?
                Colors.tableSelectionForegroundColor(cellHasFocus) :
                Colors.tableLineNumberColor());
        return this;
    }
}
