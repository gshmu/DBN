package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class BasicTableGutterCellRenderer extends JPanel implements ListCellRenderer {

    private final JLabel textLabel;

    public BasicTableGutterCellRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), Borders.TEXT_FIELD_BORDER));
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setForeground(Colors.tableLineNumberColor());
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        add(textLabel, BorderLayout.EAST);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textLabel != null) textLabel.setFont(font);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;
        textLabel.setText(Integer.toString(index + 1));
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = Failsafe.check(table) &&
                table.getCellSelectionEnabled() &&
                table.getSelectedRow() == index &&
                table.getSelectedRowCount() == 1;

        setBackground(isSelected ?
                Colors.tableSelectionBackgroundColor() :
                isCaretRow ?
                        Colors.tableCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ?
                Colors.tableSelectionBackgroundColor() :
                Colors.tableLineNumberColor());
        return this;
    }
}
